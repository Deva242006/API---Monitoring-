package com.sentinel.analytics.controller;

import com.sentinel.analytics.service.AnalyticsService;
import com.sentinel.common.dto.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardSummary() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getDashboardSummary()));
    }

    @GetMapping("/dashboard/uptime")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUptime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getUptimeStats(start, end)));
    }

    @GetMapping("/dashboard/response-time")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResponseTime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getResponseTimeStats(start, end)));
    }

    @GetMapping("/dashboard/incidents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIncidentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusHours(24);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getIncidentStats(start, end)));
    }

    @GetMapping("/apis/{id}/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getApiMetrics(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (start == null) start = LocalDateTime.now().minusDays(7);
        if (end == null) end = LocalDateTime.now();
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getApiMetrics(id, start, end)));
    }

    @GetMapping("/reports/weekly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklyReport() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getWeeklyReport()));
    }

    @GetMapping("/reports/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyReport() {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getMonthlyReport()));
    }
}

