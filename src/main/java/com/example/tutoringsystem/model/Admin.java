package com.example.tutoringsystem.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends PortalAccount {

    public Admin() {
    }

    public Admin(String name, String email, String password, boolean active) {
        super(name, email, password, UserRole.ADMIN, ApprovalStatus.APPROVED, active);
    }
}
