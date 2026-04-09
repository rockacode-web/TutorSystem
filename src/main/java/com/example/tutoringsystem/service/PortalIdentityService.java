package com.example.tutoringsystem.service;

import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.Admin;
import com.example.tutoringsystem.model.PortalAccount;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.repository.AdminRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class PortalIdentityService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;

    public PortalIdentityService(AdminRepository adminRepository,
            StudentRepository studentRepository,
            TutorRepository tutorRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
    }

    public boolean emailExists(String email) {
        return studentRepository.existsByEmailIgnoreCase(email)
                || tutorRepository.existsByEmailIgnoreCase(email)
                || adminRepository.existsByEmailIgnoreCase(email);
    }

    public Student getStudentByUsername(String username) {
        return studentRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No student record found for username: " + username));
    }

    public Tutor getTutorByUsername(String username) {
        return tutorRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No tutor record found for username: " + username));
    }

    public Admin getAdminByUsername(String username) {
        return adminRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No admin record found for username: " + username));
    }

    public PortalAccount getAccountByUsername(String username) {
        return studentRepository.findByEmailIgnoreCase(username)
                .map(account -> (PortalAccount) account)
                .or(() -> tutorRepository.findByEmailIgnoreCase(username).map(account -> (PortalAccount) account))
                .or(() -> adminRepository.findByEmailIgnoreCase(username).map(account -> (PortalAccount) account))
                .orElseThrow(() -> new RuntimeException("No portal account found for username: " + username));
    }
}
