package com.example.tutoringsystem.dto;

import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.UserRole;

public class AdminUserView {

    private final Long id;
    private final String name;
    private final String email;
    private final UserRole role;
    private final ApprovalStatus approvalStatus;
    private final boolean active;
    private final String detail;

    public AdminUserView(Long id,
            String name,
            String email,
            UserRole role,
            ApprovalStatus approvalStatus,
            boolean active,
            String detail) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.approvalStatus = approvalStatus;
        this.active = active;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public boolean isActive() {
        return active;
    }

    public String getDetail() {
        return detail;
    }
}
