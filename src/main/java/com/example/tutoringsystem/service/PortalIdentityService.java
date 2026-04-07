package com.example.tutoringsystem.service;

import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class PortalIdentityService {

    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;

    public PortalIdentityService(StudentRepository studentRepository, TutorRepository tutorRepository) {
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
    }

    public Student getStudentByUsername(String username) {
        return studentRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No student record found for username: " + username));
    }

    public Tutor getTutorByUsername(String username) {
        return tutorRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No tutor record found for username: " + username));
    }
}
