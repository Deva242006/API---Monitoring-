package com.sentinel.scheduler;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.common.enums.HttpMethodType;
import com.sentinel.healthcheck.entity.HealthCheck;
import com.sentinel.healthcheck.service.HealthCheckService;
import com.sentinel.incident.service.IncidentService;
import com.sentinel.monitoring.entity.MonitoredApi;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import com.sentinel.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * Core monitoring engine that periodically checks all active APIs.
 * Uses a bounded thread pool to execute checks concurrently.
 * Prevents overlapping checks for the same API using a concurrent lock map.
 */
@Service
public class MonitoringEngine {

    private static final Logger log = LoggerFactory.getLogger(MonitoringEngine.class);

    private final MonitoredApiRepository apiRepository;
    private final HealthCheckService healthCheckService;
    private final IncidentService incidentService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final Executor monitoringExecutor;

    // Prevents overlapping checks for the same API
    private final Map<Long, Boolean> activeChecks = new ConcurrentHashMap<>();

    @Value("${monitoring.failure-threshold:3}")
    private int failureThreshold;

    @Value("${monitoring.max-timeout-ms:30000}")
    private int maxTimeoutMs;

    public MonitoringEngine(MonitoredApiRepository apiRepository,
                            HealthCheckService healthCheckService,
                            IncidentService incidentService,
                            NotificationService notificationService,
                            RestTemplate restTemplate,
                            SimpMessagingTemplate messagingTemplate,
                            @Qualifier("monitoringExecutor") Executor monitoringExecutor) {
        this.apiRepository = apiRepository;
        this.healthCheckService = healthCheckService;
        this.incidentService = incidentService;
        this.notificationService = notificationService;
        this.restTemplate = restTemplate;
        this.messagingTemplate = messagingTemplate;
        this.monitoringExecutor = monitoringExecutor;
    }

    /**
     * Runs every 15 seconds. Checks each active API based on its configured interval.
     */
    @Scheduled(fixedDelayString = "${monitoring.default-interval-seconds:15}000")
    public void runScheduledChecks() {
        List<MonitoredApi> activeApis = apiRepository.findByActiveTrue();
        LocalDateTime now = LocalDateTime.now();

        for (MonitoredApi api : activeApis) {
            if (shouldCheck(api, now)) {
                monitoringExecutor.execute(() -> performHealthCheck(api));
            }
        }
    }

    /**
     * Manually trigger a health check for a specific API.
     */
    public void triggerManualCheck(MonitoredApi api) {
        monitoringExecutor.execute(() -> performHealthCheck(api));
    }

    private boolean shouldCheck(MonitoredApi api, LocalDateTime now) {
        if (api.getLastCheckedAt() == null) return true;
        LocalDateTime nextCheck = api.getLastCheckedAt().plusSeconds(api.getMonitoringIntervalSeconds());
        return now.isAfter(nextCheck) || now.isEqual(nextCheck);
    }

    private void performHealthCheck(MonitoredApi api) {
        // Prevent overlapping checks
        if (activeChecks.putIfAbsent(api.getId(), true) != null) {
            log.debug("Skipping check for API {} - already in progress", api.getName());
            return;
        }

        try {
            doCheck(api);
        } finally {
            activeChecks.remove(api.getId());
        }
    }

    private void doCheck(MonitoredApi api) {
        long startTime = System.currentTimeMillis();
        ApiStatus status;
        Integer responseStatusCode = null;
        String errorMessage = null;
        long responseTime;

        try {
            int timeout = Math.min(api.getTimeoutMs(), maxTimeoutMs);
            org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            RestTemplate timeoutTemplate = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            HttpMethod method = switch (api.getHttpMethod()) {
                case POST -> HttpMethod.POST;
                case PUT -> HttpMethod.PUT;
                case DELETE -> HttpMethod.DELETE;
                default -> HttpMethod.GET;
            };

            ResponseEntity<String> response = timeoutTemplate.exchange(
                    api.getEndpointUrl(), method, entity, String.class);

            responseTime = System.currentTimeMillis() - startTime;
            responseStatusCode = response.getStatusCode().value();

            if (responseStatusCode == api.getExpectedStatusCode()) {
                status = responseTime > (api.getTimeoutMs() * 0.8) ? ApiStatus.DEGRADED : ApiStatus.HEALTHY;
            } else {
                status = ApiStatus.DOWN;
                errorMessage = "Unexpected status code: " + responseStatusCode + " (expected: " + api.getExpectedStatusCode() + ")";
            }
        } catch (org.springframework.web.client.ResourceAccessException e) {
            responseTime = System.currentTimeMillis() - startTime;
            status = ApiStatus.DOWN;
            errorMessage = "Connection failed: " + e.getMostSpecificCause().getMessage();
            log.warn("Health check failed for API {}: {}", api.getName(), errorMessage);
        } catch (Exception e) {
            responseTime = System.currentTimeMillis() - startTime;
            status = ApiStatus.DOWN;
            errorMessage = "Error: " + e.getMessage();
            log.error("Health check error for API {}: {}", api.getName(), e.getMessage());
        }

        // Record health check result (outside the HTTP transaction)
        processCheckResult(api.getId(), status, responseTime, responseStatusCode, errorMessage);
    }

    @Transactional
    public void processCheckResult(Long apiId, ApiStatus status, long responseTime,
                                    Integer statusCode, String errorMessage) {
        MonitoredApi api = apiRepository.findById(apiId).orElse(null);
        if (api == null) return;

        ApiStatus previousStatus = api.getCurrentStatus();

        // Record the health check
        HealthCheck healthCheck = healthCheckService.recordHealthCheck(api, status, responseTime, statusCode, errorMessage);

        // Update API status
        api.setCurrentStatus(status);
        api.setLastCheckedAt(LocalDateTime.now());
        api.setLastResponseTimeMs(responseTime);

        if (status == ApiStatus.DOWN) {
            api.setConsecutiveFailures(api.getConsecutiveFailures() + 1);

            // Create incident after threshold consecutive failures
            if (api.getConsecutiveFailures() >= failureThreshold) {
                incidentService.createAutoIncident(api);
            }
        } else {
            // Recovery detected
            if (previousStatus == ApiStatus.DOWN && api.getConsecutiveFailures() > 0) {
                notificationService.notifyApiRecovery(api);
                incidentService.resolveAutoIncidents(api);
            }
            api.setConsecutiveFailures(0);
        }

        apiRepository.save(api);

        // Push real-time WebSocket update
        try {
            messagingTemplate.convertAndSend("/topic/health-updates",
                    Map.of("apiId", api.getId(), "apiName", api.getName(),
                            "status", status.name(), "responseTime", responseTime,
                            "checkedAt", LocalDateTime.now().toString()));

            if (status == ApiStatus.DOWN && previousStatus != ApiStatus.DOWN) {
                messagingTemplate.convertAndSend("/topic/incidents",
                        Map.of("type", "API_DOWN", "apiName", api.getName(), "apiId", api.getId()));
            } else if (status != ApiStatus.DOWN && previousStatus == ApiStatus.DOWN) {
                messagingTemplate.convertAndSend("/topic/incidents",
                        Map.of("type", "API_RECOVERED", "apiName", api.getName(), "apiId", api.getId()));
            }
        } catch (Exception e) {
            log.debug("WebSocket send failed (no subscribers): {}", e.getMessage());
        }
    }
}

