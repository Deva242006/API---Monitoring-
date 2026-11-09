package com.sentinel.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(max = 200)
    private String name;
    @Size(max = 2000)
    private String description;
    private List<Long> memberIds;

    public CreateProjectRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}

