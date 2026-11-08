package com.sentinel.user.dto;

import com.sentinel.common.enums.UserRole;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private Boolean active;
    private String avatarUrl;
    private String phone;
    private String createdAt;
    private String lastLoginAt;

    public UserDto() {}
    public UserDto(Long id, String email, String fullName, UserRole role, Boolean active,
                   String avatarUrl, String phone, String createdAt, String lastLoginAt) {
        this.id = id; this.email = email; this.fullName = fullName; this.role = role;
        this.active = active; this.avatarUrl = avatarUrl; this.phone = phone;
        this.createdAt = createdAt; this.lastLoginAt = lastLoginAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
