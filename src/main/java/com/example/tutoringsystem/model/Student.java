package com.example.tutoringsystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Student extends PortalAccount {

    @Column(nullable = false)
    private String idNumber;

    public Student() {
    }

    public Student(String name,
            String email,
            String password,
            String idNumber,
            ApprovalStatus approvalStatus,
            boolean active) {
        super(name, email, password, UserRole.STUDENT, approvalStatus, active);
        this.idNumber = idNumber;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }
}
