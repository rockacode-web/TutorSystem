package com.example.tutoringsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.TutoringSession;

public interface TutoringSessionRepository extends JpaRepository<TutoringSession, Long> {


    List<TutoringSession> findByStudentId(Long studentId);
    List<TutoringSession> findByTutorId(Long tutorId);
    List<TutoringSession> findByTutorIdOrderByDateAscStartTimeAsc(Long tutorId);
    List<TutoringSession> findByStudentIdOrderByDateAscStartTimeAsc(Long studentId);

    long countByStatus(SessionStatus status);
}
