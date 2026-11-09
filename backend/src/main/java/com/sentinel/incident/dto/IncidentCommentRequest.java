package com.sentinel.incident.dto;

import jakarta.validation.constraints.NotBlank;

public class IncidentCommentRequest {
    @NotBlank(message = "Comment content is required")
    private String content;

    public IncidentCommentRequest() {}
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}

