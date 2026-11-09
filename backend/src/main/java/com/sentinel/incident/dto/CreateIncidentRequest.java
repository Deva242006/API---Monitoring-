package com.sentinel.incident.dto;

import com.sentinel.common.enums.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateIncidentRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "API ID is required")
    private Long apiId;
    private IncidentSeverity severity = IncidentSeverity.MEDIUM;
    private Long assignedToId;

    public CreateIncidentRequest() {}
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getApiId() { return apiId; }
    public void setApiId(Long apiId) { this.apiId = apiId; }
    public IncidentSeverity getSeverity() { return severity; }
    public void setSeverity(IncidentSeverity severity) { this.severity = severity; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
}

