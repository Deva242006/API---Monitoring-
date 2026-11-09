package com.sentinel.healthcheck.dto;

import com.sentinel.common.enums.ApiStatus;

public class HealthCheckDto {
    private Long id;
    private Long apiId;
    private String apiName;
    private ApiStatus status;
    private Long responseTimeMs;
    private Integer statusCode;
    private String errorMessage;
    private String checkedAt;

    public HealthCheckDto() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }
    public String getApiName() { return apiName; }
    public void setApiName(String apiName) { this.apiName = apiName; }
    public ApiStatus getStatus() { return status; }
    public void setStatus(ApiStatus status) { this.status = status; }
    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getCheckedAt() { return checkedAt; }
    public void setCheckedAt(String checkedAt) { this.checkedAt = checkedAt; }
}

