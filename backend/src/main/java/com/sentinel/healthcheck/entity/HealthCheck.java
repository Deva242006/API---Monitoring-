package com.sentinel.healthcheck.entity;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.monitoring.entity.MonitoredApi;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_checks")
public class HealthCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private MonitoredApi api;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApiStatus status;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "checked_at", nullable = false, updatable = false)
    private LocalDateTime checkedAt;

    @PrePersist
    protected void onCreate() {
        checkedAt = LocalDateTime.now();
    }

    public HealthCheck() {}

    public HealthCheck(MonitoredApi api, ApiStatus status, Long responseTimeMs, Integer statusCode, String errorMessage) {
        this.api = api;
        this.status = status;
        this.responseTimeMs = responseTimeMs;
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MonitoredApi getApi() { return api; }
    public void setApi(MonitoredApi api) { this.api = api; }
    public ApiStatus getStatus() { return status; }
    public void setStatus(ApiStatus status) { this.status = status; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}
