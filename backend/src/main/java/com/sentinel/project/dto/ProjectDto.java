package com.sentinel.project.dto;

import com.sentinel.user.dto.UserDto;
import java.util.List;

public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private UserDto owner;
    private Boolean active;
    private List<UserDto> members;
    private int apiCount;
    private String createdAt;
    private String updatedAt;

    public ProjectDto() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UserDto getOwner() { return owner; }
    public void setOwner(UserDto owner) { this.owner = owner; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public List<UserDto> getMembers() { return members; }
    public void setMembers(List<UserDto> members) { this.members = members; }
    public int getApiCount() { return apiCount; }
    public void setApiCount(int apiCount) { this.apiCount = apiCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
