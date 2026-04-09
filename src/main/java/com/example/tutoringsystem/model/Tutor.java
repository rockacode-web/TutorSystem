package com.example.tutoringsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Tutor extends PortalAccount {

    @Column(nullable = false)
    private String subject;

    public Tutor() {
    }

    public Tutor(String name,
            String email,
            String password,
            String subject,
            ApprovalStatus approvalStatus,
            boolean active) {
        super(name, email, password, UserRole.TUTOR, approvalStatus, active);
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
