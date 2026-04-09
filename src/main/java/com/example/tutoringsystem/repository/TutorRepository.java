package com.example.tutoringsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Long> {

    Optional<Tutor> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Tutor> findAllByOrderByNameAsc();
}
