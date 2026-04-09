package com.example.tutoringsystem.service;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.Admin;
import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.PortalAccount;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.repository.AdminRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class PortalUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;

    public PortalUserDetailsService(AdminRepository adminRepository,
            StudentRepository studentRepository,
            TutorRepository tutorRepository) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        PortalAccount account = studentRepository.findByEmailIgnoreCase(username)
                .map(user -> (PortalAccount) user)
                .or(() -> tutorRepository.findByEmailIgnoreCase(username).map(user -> (PortalAccount) user))
                .or(() -> adminRepository.findByEmailIgnoreCase(username).map(user -> (PortalAccount) user))
                .orElseThrow(() -> new UsernameNotFoundException("No portal account found for that email address."));

        validateAccess(account);
        return buildUser(account);
    }

    private UserDetails buildUser(PortalAccount account) {
        return User.withUsername(account.getEmail())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .build();
    }

    private void validateAccess(PortalAccount account) {
        if (!account.isActive()) {
            throw new DisabledException("Your account has been deactivated. Please contact an administrator.");
        }

        if (account.getApprovalStatus() == ApprovalStatus.PENDING) {
            throw new LockedException("Your registration is pending admin approval.");
        }

        if (account.getApprovalStatus() == ApprovalStatus.REJECTED) {
            throw new LockedException("Your registration was rejected. Please contact an administrator.");
        }
    }
}
