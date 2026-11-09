package com.sentinel.monitoring.dto;

import com.sentinel.common.enums.ApiStatus;
import com.sentinel.common.enums.HttpMethodType;

public class MonitoredApiDto {
    private Long id;
    private String name;
    private String endpointUrl;
    private HttpMethodType httpMethod;
    private Integer expectedStatusCode;
    private Integer timeoutMs;
    private Integer monitoringIntervalSeconds;
    private Long projectId;
    private String projectName;
    private Boolean active;
    private String description;
    private ApiStatus currentStatus;
    private Integer consecutiveFailures;
    private String lastCheckedAt;
    private Long lastResponseTimeMs;
    private String createdAt;

    public MonitoredApiDto() {}
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
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ApiStatus getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(ApiStatus currentStatus) { this.currentStatus = currentStatus; }
    public Integer getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(Integer consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public String getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(String lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Long getLastResponseTimeMs() { return lastResponseTimeMs; }
    public void setLastResponseTimeMs(Long lastResponseTimeMs) { this.lastResponseTimeMs = lastResponseTimeMs; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

