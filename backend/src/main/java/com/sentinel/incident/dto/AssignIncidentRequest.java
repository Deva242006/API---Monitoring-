package com.sentinel.incident.dto;

import jakarta.validation.constraints.NotNull;

public class AssignIncidentRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    public AssignIncidentRequest() {}
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

