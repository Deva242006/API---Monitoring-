package com.sentinel.project.dto;

import jakarta.validation.constraints.Size;

public class UpdateProjectRequest {
    @Size(max = 200) private String name;
    @Size(max = 2000) private String description;
    private Boolean active;

    public UpdateProjectRequest() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}

