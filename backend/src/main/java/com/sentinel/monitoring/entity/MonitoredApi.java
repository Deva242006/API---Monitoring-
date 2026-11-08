package com.sentinel.monitoring.entity;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.common.enums.HttpMethodType;
import com.sentinel.project.entity.Project;
import com.sentinel.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "monitored_apis")
public class MonitoredApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "endpoint_url", nullable = false, length = 2000)
    private String endpointUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false)
    private HttpMethodType httpMethod = HttpMethodType.GET;

    @Column(name = "expected_status_code", nullable = false)
    private Integer expectedStatusCode = 200;

    @Column(name = "timeout_ms", nullable = false)
    private Integer timeoutMs = 5000;

    @Column(name = "monitoring_interval_seconds", nullable = false)
    private Integer monitoringIntervalSeconds = 60;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "headers_json", columnDefinition = "TEXT")
    private String headersJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false)
    private ApiStatus currentStatus = ApiStatus.UNKNOWN;

    @Column(name = "consecutive_failures", nullable = false)
    private Integer consecutiveFailures = 0;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "last_response_time_ms")
    private Long lastResponseTimeMs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MonitoredApi() {}

    // All Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public HttpMethodType getHttpMethod() { return httpMethod; }
    public void setHttpMethod(HttpMethodType httpMethod) { this.httpMethod = httpMethod; }
    public Integer getExpectedStatusCode() { return expectedStatusCode; }
    public void setExpectedStatusCode(Integer expectedStatusCode) { this.expectedStatusCode = expectedStatusCode; }
    public Integer getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(Integer timeoutMs) { this.timeoutMs = timeoutMs; }
    public Integer getMonitoringIntervalSeconds() { return monitoringIntervalSeconds; }
    public void setMonitoringIntervalSeconds(Integer monitoringIntervalSeconds) { this.monitoringIntervalSeconds = monitoringIntervalSeconds; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }
    public ApiStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(ApiStatus currentStatus) { this.currentStatus = currentStatus; }
    public Integer getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(Integer consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public LocalDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(LocalDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Long getLastResponseTimeMs() { return lastResponseTimeMs; }
    public void setLastResponseTimeMs(Long lastResponseTimeMs) { this.lastResponseTimeMs = lastResponseTimeMs; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
