package com.example.tutoringsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Student> findByApprovalStatusOrderByNameAsc(ApprovalStatus approvalStatus);

    List<Student> findAllByOrderByNameAsc();
}
