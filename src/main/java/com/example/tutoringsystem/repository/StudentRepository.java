package com.example.tutoringsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
