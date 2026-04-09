package com.example.tutoringsystem.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.dto.StudentRegistrationForm;
import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.repository.StudentRepository;

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final PortalIdentityService portalIdentityService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public RegistrationService(StudentRepository studentRepository,
            PortalIdentityService portalIdentityService,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.portalIdentityService = portalIdentityService;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    public Student registerStudent(StudentRegistrationForm form) {
        if (portalIdentityService.emailExists(form.getEmail())) {
            throw new RuntimeException("An account with that email address already exists.");
        }

        Student student = new Student(
                form.getName().trim(),
                form.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(form.getPassword()),
                form.getIdNumber().trim(),
                ApprovalStatus.PENDING,
                true);

        Student savedStudent = studentRepository.save(student);
        notificationService.notifyRegistrationSubmitted(savedStudent);
        return savedStudent;
    }
}
