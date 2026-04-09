package com.example.tutoringsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tutoringsystem.model.NotificationRecord;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, Long> {

    List<NotificationRecord> findTop5ByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String recipientEmail);
}
