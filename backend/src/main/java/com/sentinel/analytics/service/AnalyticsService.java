package com.sentinel.analytics.service;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.healthcheck.repository.HealthCheckRepository;
import com.sentinel.incident.repository.IncidentRepository;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AnalyticsService {

    private final MonitoredApiRepository apiRepository;
    private final HealthCheckRepository healthCheckRepository;
    private final IncidentRepository incidentRepository;

    public AnalyticsService(MonitoredApiRepository apiRepository,
                            HealthCheckRepository healthCheckRepository,
                            IncidentRepository incidentRepository) {
        this.apiRepository = apiRepository;
        this.healthCheckRepository = healthCheckRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalApis", apiRepository.countActive());
        summary.put("healthyApis", apiRepository.countByCurrentStatusAndActive(ApiStatus.HEALTHY));
        summary.put("degradedApis", apiRepository.countByCurrentStatusAndActive(ApiStatus.DEGRADED));
        summary.put("downApis", apiRepository.countByCurrentStatusAndActive(ApiStatus.DOWN));
        summary.put("activeIncidents", incidentRepository.countActive());
        summary.put("resolvedIncidents", incidentRepository.countResolved());

        // Uptime and response time for last 24h
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24h = now.minusHours(24);

        long totalChecks = healthCheckRepository.countByDateRange(last24h, now);
        long healthyChecks = healthCheckRepository.countHealthyByDateRange(last24h, now);
        double uptimePercentage = totalChecks > 0 ? (healthyChecks * 100.0 / totalChecks) : 0;
        summary.put("uptimePercentage", Math.round(uptimePercentage * 100.0) / 100.0);

        Double avgResponseTime = healthCheckRepository.findOverallAvgResponseTime(last24h, now);
        summary.put("avgResponseTime", avgResponseTime != null ? Math.round(avgResponseTime * 100.0) / 100.0 : 0);

        long failedChecks = healthCheckRepository.countFailedByDateRange(last24h, now);
        summary.put("failedChecks", failedChecks);
        summary.put("totalChecks", totalChecks);

        return summary;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUptimeStats(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalChecks = healthCheckRepository.countByDateRange(start, end);
        long healthyChecks = healthCheckRepository.countHealthyByDateRange(start, end);
        double uptimePercentage = totalChecks > 0 ? (healthyChecks * 100.0 / totalChecks) : 0;

        stats.put("uptimePercentage", Math.round(uptimePercentage * 100.0) / 100.0);
        stats.put("totalChecks", totalChecks);
        stats.put("healthyChecks", healthyChecks);
        stats.put("failedChecks", healthCheckRepository.countFailedByDateRange(start, end));
        stats.put("start", start.toString());
        stats.put("end", end.toString());

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getResponseTimeStats(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new LinkedHashMap<>();

        Double avgResponseTime = healthCheckRepository.findOverallAvgResponseTime(start, end);
        stats.put("averageResponseTime", avgResponseTime != null ? Math.round(avgResponseTime * 100.0) / 100.0 : 0);

        // Calculate P95
        List<Long> responseTimes = healthCheckRepository.findAllResponseTimesInRange(start, end);
        if (!responseTimes.isEmpty()) {
            int p95Index = (int) Math.ceil(responseTimes.size() * 0.95) - 1;
            stats.put("p95ResponseTime", responseTimes.get(Math.max(0, p95Index)));
            stats.put("minResponseTime", responseTimes.get(0));
            stats.put("maxResponseTime", responseTimes.get(responseTimes.size() - 1));
        } else {
            stats.put("p95ResponseTime", 0);
            stats.put("minResponseTime", 0);
            stats.put("maxResponseTime", 0);
        }
        stats.put("sampleCount", responseTimes.size());

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getIncidentStats(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalIncidents", incidentRepository.countByDateRange(start, end));
        stats.put("resolvedIncidents", incidentRepository.countResolvedByDateRange(start, end));
        stats.put("activeIncidents", incidentRepository.countActive());
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        return buildReport(weekAgo, now, "Weekly");
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyReport() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minusDays(30);
        return buildReport(monthAgo, now, "Monthly");
    }

    private Map<String, Object> buildReport(LocalDateTime start, LocalDateTime end, String period) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("period", period);
        report.put("start", start.toString());
        report.put("end", end.toString());
        report.put("uptime", getUptimeStats(start, end));
        report.put("responseTime", getResponseTimeStats(start, end));
        report.put("incidents", getIncidentStats(start, end));
        report.put("totalApis", apiRepository.countActive());
        return report;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getApiMetrics(Long apiId, LocalDateTime start, LocalDateTime end) {
        Map<String, Object> metrics = new LinkedHashMap<>();

        long totalChecks = healthCheckRepository.countByApiIdAndDateRange(apiId, start, end);
        long healthyChecks = healthCheckRepository.countByApiIdAndStatusAndDateRange(apiId, ApiStatus.HEALTHY, start, end);
        double uptime = totalChecks > 0 ? (healthyChecks * 100.0 / totalChecks) : 0;

        metrics.put("uptimePercentage", Math.round(uptime * 100.0) / 100.0);
        metrics.put("totalChecks", totalChecks);
        metrics.put("healthyChecks", healthyChecks);

        Double avgResponseTime = healthCheckRepository.findAvgResponseTime(apiId, start, end);
        metrics.put("avgResponseTime", avgResponseTime != null ? Math.round(avgResponseTime * 100.0) / 100.0 : 0);

        return metrics;
    }
}

