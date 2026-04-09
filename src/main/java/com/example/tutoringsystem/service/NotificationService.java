package com.example.tutoringsystem.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.NotificationRecord;
import com.example.tutoringsystem.model.PortalAccount;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.NotificationRecordRepository;

@Service
public class NotificationService {

    private static final DateTimeFormatter SESSION_FORMAT = DateTimeFormatter.ofPattern("MMM d yyyy 'at' h:mm a");

    private final NotificationRecordRepository notificationRecordRepository;

    public NotificationService(NotificationRecordRepository notificationRecordRepository) {
        this.notificationRecordRepository = notificationRecordRepository;
    }

    public void notifyRegistrationSubmitted(Student student) {
        createNotification(
                null,
                student,
                "Registration submitted for approval. An administrator will review your student account.");
    }

    public void notifyBookingConfirmed(TutoringSession session) {
        String message = "Booking confirmed for "
                + session.getDate().atTime(session.getStartTime()).format(SESSION_FORMAT)
                + " with "
                + session.getTutor().getName()
                + ".";
        createNotification(session, session.getStudent(), message);
        createNotification(session, session.getTutor(),
                "A new tutoring session has been booked with " + session.getStudent().getName() + ".");
    }

    public void notifySessionCancelled(TutoringSession session, String cancelledByName) {
        String message = "Session on "
                + session.getDate().atTime(session.getStartTime()).format(SESSION_FORMAT)
                + " was cancelled by "
                + cancelledByName
                + ".";
        createNotification(session, session.getStudent(), message);
        createNotification(session, session.getTutor(), message);
    }

    public List<NotificationRecord> getRecentNotifications(String email) {
        return notificationRecordRepository.findTop5ByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    private void createNotification(TutoringSession session, PortalAccount recipient, String message) {
        notificationRecordRepository.save(new NotificationRecord(
                session,
                recipient.getName(),
                recipient.getEmail(),
                recipient.getRole(),
                message,
                LocalDateTime.now()));
    }
}
