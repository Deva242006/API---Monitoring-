package com.sentinel.analytics.service;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.healthcheck.repository.HealthCheckRepository;
import com.sentinel.incident.repository.IncidentRepository;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private MonitoredApiRepository apiRepository;

    @Mock
    private HealthCheckRepository healthCheckRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Should calculate uptime and summary statistics accurately")
    void testGetDashboardSummary() {
        when(apiRepository.countActive()).thenReturn(5L);
        when(apiRepository.countByCurrentStatusAndActive(ApiStatus.HEALTHY)).thenReturn(4L);
        when(apiRepository.countByCurrentStatusAndActive(ApiStatus.DEGRADED)).thenReturn(0L);
        when(apiRepository.countByCurrentStatusAndActive(ApiStatus.DOWN)).thenReturn(1L);
        when(incidentRepository.countActive()).thenReturn(1L);
        when(incidentRepository.countResolved()).thenReturn(3L);

        when(healthCheckRepository.countByDateRange(any(), any())).thenReturn(100L);
        when(healthCheckRepository.countHealthyByDateRange(any(), any())).thenReturn(95L);
        when(healthCheckRepository.findOverallAvgResponseTime(any(), any())).thenReturn(145.5);
        when(healthCheckRepository.countFailedByDateRange(any(), any())).thenReturn(5L);

        Map<String, Object> summary = analyticsService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(5L, summary.get("totalApis"));
        assertEquals(4L, summary.get("healthyApis"));
        assertEquals(1L, summary.get("downApis"));
        assertEquals(95.0, summary.get("uptimePercentage"));
        assertEquals(145.5, summary.get("avgResponseTime"));
    }

    @Test
    @DisplayName("Should correctly calculate P95 response time")
    void testGetResponseTimeStatsP95() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        // 20 sorted values: 10, 20, 30 ... 200
        List<Long> responseTimes = List.of(
                10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L,
                110L, 120L, 130L, 140L, 150L, 160L, 170L, 180L, 190L, 200L
        );

        when(healthCheckRepository.findOverallAvgResponseTime(yesterday, now)).thenReturn(105.0);
        when(healthCheckRepository.findAllResponseTimesInRange(yesterday, now)).thenReturn(responseTimes);

        Map<String, Object> stats = analyticsService.getResponseTimeStats(yesterday, now);

        assertNotNull(stats);
        assertEquals(105.0, stats.get("averageResponseTime"));
        assertEquals(190L, stats.get("p95ResponseTime"));
        assertEquals(10L, stats.get("minResponseTime"));
        assertEquals(200L, stats.get("maxResponseTime"));
        assertEquals(20, stats.get("sampleCount"));
    }
}

