package com.example.tutoringsystem.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.SessionSlot;

public interface SessionSlotRepository extends JpaRepository<SessionSlot, Long> {

    List<SessionSlot> findByAvailableTrueOrderByDateAscStartTimeAsc();

    List<SessionSlot> findByTutorIdOrderByDateAscStartTimeAsc(Long tutorId);

    List<SessionSlot> findByTutorIdAndDateOrderByStartTimeAsc(Long tutorId, LocalDate date);

    long countByAvailableTrue();
}
