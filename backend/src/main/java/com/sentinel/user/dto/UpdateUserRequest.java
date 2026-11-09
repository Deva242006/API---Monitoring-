package com.sentinel.user.dto;

import com.sentinel.common.enums.UserRole;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {
    @Size(min = 2, max = 150)
    private String fullName;
    private UserRole role;
    private Boolean active;
    private String phone;

    public UpdateUserRequest() {}
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

