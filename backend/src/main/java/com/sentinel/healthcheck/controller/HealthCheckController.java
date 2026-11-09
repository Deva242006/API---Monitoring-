package com.sentinel.healthcheck.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.dto.PagedResponse;
import com.sentinel.healthcheck.dto.HealthCheckDto;
import com.sentinel.healthcheck.service.HealthCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    public HealthCheckController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @GetMapping("/apis/{id}/health")
    public ResponseEntity<ApiResponse<List<HealthCheckDto>>> getRecentHealth(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(healthCheckService.getRecentChecks(id)));
    }

    @GetMapping("/apis/{id}/history")
    public ResponseEntity<ApiResponse<PagedResponse<HealthCheckDto>>> getHealthHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(healthCheckService.getHealthHistory(id, page, size)));
    }

    @GetMapping("/health-checks")
    public ResponseEntity<ApiResponse<PagedResponse<HealthCheckDto>>> getAllHealthChecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(healthCheckService.getAllHealthChecks(page, size)));
    }
}

