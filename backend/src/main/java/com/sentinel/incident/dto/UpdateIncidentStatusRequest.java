package com.sentinel.incident.dto;

import com.sentinel.common.enums.IncidentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateIncidentStatusRequest {
    @NotNull(message = "Status is required")
    private IncidentStatus status;

    public UpdateIncidentStatusRequest() {}
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
}

