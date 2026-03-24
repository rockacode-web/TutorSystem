package com.example.tutoringsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.SessionSlot;

public interface SessionSlotRepository extends JpaRepository<SessionSlot, Long> {

    List<SessionSlot> findByAvailableTrue();
}
