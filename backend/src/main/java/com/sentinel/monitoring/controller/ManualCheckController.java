package com.sentinel.monitoring.controller;

import com.sentinel.common.dto.ApiResponse;
import com.sentinel.common.exception.ResourceNotFoundException;
import com.sentinel.monitoring.entity.MonitoredApi;
import com.sentinel.monitoring.repository.MonitoredApiRepository;
import com.sentinel.scheduler.MonitoringEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/apis")
public class ManualCheckController {

    private final MonitoredApiRepository apiRepository;
    private final MonitoringEngine monitoringEngine;

    public ManualCheckController(MonitoredApiRepository apiRepository, MonitoringEngine monitoringEngine) {
        this.apiRepository = apiRepository;
        this.monitoringEngine = monitoringEngine;
    }

    @PostMapping("/{id}/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<String>> triggerManualCheck(@PathVariable Long id) {
        MonitoredApi api = apiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API", "id", id));
        monitoringEngine.triggerManualCheck(api);
        return ResponseEntity.ok(ApiResponse.success("Health check triggered for: " + api.getName()));
    }
}

