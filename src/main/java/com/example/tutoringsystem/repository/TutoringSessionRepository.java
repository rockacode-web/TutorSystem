package com.example.tutoringsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.TutoringSession;

public interface TutoringSessionRepository extends JpaRepository<TutoringSession, Long> {

    List<TutoringSession> findByTutorId(Long tutorId);
}
