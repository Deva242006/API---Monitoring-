package com.sentinel.monitoring.dto;

import com.sentinel.common.enums.HttpMethodType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UpdateApiRequest {
    @Size(max = 200) private String name;
    @Size(max = 2000) private String endpointUrl;
    private HttpMethodType httpMethod;
    @Min(100) @Max(599) private Integer expectedStatusCode;
    @Min(1000) @Max(60000) private Integer timeoutMs;
    @Min(10) @Max(86400) private Integer monitoringIntervalSeconds;
    private String description;
    private String headersJson;

    public UpdateApiRequest() {}
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }
}
