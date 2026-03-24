package com.example.tutoringsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
}
