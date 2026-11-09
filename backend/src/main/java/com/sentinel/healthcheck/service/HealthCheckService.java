package com.sentinel.healthcheck.service;

import com.sentinel.common.dto.PagedResponse;
import com.sentinel.common.enums.ApiStatus;
import com.sentinel.healthcheck.dto.HealthCheckDto;
import com.sentinel.healthcheck.entity.HealthCheck;
import com.sentinel.healthcheck.repository.HealthCheckRepository;
import com.sentinel.monitoring.entity.MonitoredApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthCheckService {

    private final HealthCheckRepository healthCheckRepository;

    public HealthCheckService(HealthCheckRepository healthCheckRepository) {
        this.healthCheckRepository = healthCheckRepository;
    }

    @Transactional
    public HealthCheck recordHealthCheck(MonitoredApi api, ApiStatus status, Long responseTimeMs,
                                          Integer statusCode, String errorMessage) {
        HealthCheck healthCheck = new HealthCheck(api, status, responseTimeMs, statusCode, errorMessage);
        return healthCheckRepository.save(healthCheck);
    }

    @Transactional(readOnly = true)
    public PagedResponse<HealthCheckDto> getHealthHistory(Long apiId, int page, int size) {
        Page<HealthCheck> checks = healthCheckRepository.findByApiIdOrderByCheckedAtDesc(
                apiId, PageRequest.of(page, size));
        return toPagedResponse(checks);
    }

    @Transactional(readOnly = true)
    public List<HealthCheckDto> getRecentChecks(Long apiId) {
        return healthCheckRepository.findTop10ByApiIdOrderByCheckedAtDesc(apiId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<HealthCheckDto> getAllHealthChecks(int page, int size) {
        Page<HealthCheck> checks = healthCheckRepository.findAllByOrderByCheckedAtDesc(
                PageRequest.of(page, size));
        return toPagedResponse(checks);
    }

    @Transactional(readOnly = true)
    public List<HealthCheck> getHealthChecksInRange(Long apiId, LocalDateTime start, LocalDateTime end) {
        return healthCheckRepository.findByApiIdAndDateRange(apiId, start, end);
    }

    public HealthCheckDto toDto(HealthCheck hc) {
        HealthCheckDto dto = new HealthCheckDto();
        dto.setId(hc.getId());
        dto.setApiId(hc.getApi().getId());
        dto.setApiName(hc.getApi().getName());
        dto.setStatus(hc.getStatus());
        dto.setResponseTimeMs(hc.getResponseTimeMs());
        dto.setStatusCode(hc.getStatusCode());
        dto.setErrorMessage(hc.getErrorMessage());
        dto.setCheckedAt(hc.getCheckedAt() != null ? hc.getCheckedAt().toString() : null);
        return dto;
    }

    private PagedResponse<HealthCheckDto> toPagedResponse(Page<HealthCheck> page) {
        return new PagedResponse<>(page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}

