package com.sentinel.project.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ProjectMemberRequest {
    @NotEmpty(message = "At least one user ID is required")
    private List<Long> userIds;

    public ProjectMemberRequest() {}
    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
}
