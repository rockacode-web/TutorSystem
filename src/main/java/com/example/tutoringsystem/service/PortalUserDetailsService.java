package com.example.tutoringsystem.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.config.DemoAccounts;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    public PortalUserDetailsService(StudentRepository studentRepository,
            TutorRepository tutorRepository,
            PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (studentRepository.findByEmailIgnoreCase(username).isPresent()) {
            return buildUser(username, DemoAccounts.STUDENT_PASSWORD, "STUDENT");
        }

        if (tutorRepository.findByEmailIgnoreCase(username).isPresent()) {
            return buildUser(username, DemoAccounts.TUTOR_PASSWORD, "TUTOR");
        }

        throw new UsernameNotFoundException("No portal account found for username: " + username);
    }

    private UserDetails buildUser(String username, String rawPassword, String role) {
        return User.withUsername(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(role)
                .build();
    }
}
