package com.example.tutoringsystem.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.NotificationRecord;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.model.UserRole;
import com.example.tutoringsystem.repository.NotificationRecordRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final NotificationRecordRepository notificationRecordRepository;

    public NotificationService(JavaMailSender mailSender,
            NotificationRecordRepository notificationRecordRepository) {
        this.mailSender = mailSender;
        this.notificationRecordRepository = notificationRecordRepository;
    }

    public List<NotificationRecord> getRecentNotifications(String email) {
        return notificationRecordRepository.findTop5ByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(email);
    }

    public void notifyRegistrationSubmitted(Student student) {
        String subject = "Registration Received - TutoringSystem";
        String message = "Thank you for registering. Your account is currently pending approval. "
                + "You will be notified once an administrator reviews your application.";
        String htmlBody = "<h2>Registration Received</h2>"
                + "<p>Dear <b>" + student.getName() + "</b>,</p>"
                + "<p>" + message + "</p>"
                + "<br><p>The TutoringSystem Team</p>";
        sendEmail(student.getEmail(), subject, htmlBody);
        notificationRecordRepository.save(new NotificationRecord(
                null, student.getName(), student.getEmail(),
                UserRole.STUDENT, message, LocalDateTime.now()));
    }

    public void notifyBookingConfirmed(TutoringSession session) {
        String studentEmail = session.getStudent().getEmail();
        String subject = "Booking Confirmed - Tutoring Session #" + session.getId();
        String message = "Your tutoring session with " + session.getTutor().getName()
                + " on " + session.getDate()
                + " from " + session.getStartTime()
                + " to " + session.getEndTime() + " has been confirmed.";
        String htmlBody = "<h2>Booking Confirmed!</h2>"
                + "<p>Dear <b>" + session.getStudent().getName() + "</b>,</p>"
                + "<p>Your tutoring session has been successfully booked. Here are the details:</p>"
                + "<table style='border-collapse:collapse;width:400px;'>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Session ID</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getId() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Tutor</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getTutor().getName() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Subject</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getTutor().getSubject() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Date</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getDate() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Start Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getStartTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>End Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getEndTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Status</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;color:green;'>" + session.getStatus() + "</td></tr>"
                + "</table>"
                + "<p>Thank you for booking with us!</p>";
        sendEmail(studentEmail, subject, htmlBody);
        notificationRecordRepository.save(new NotificationRecord(
                session, session.getStudent().getName(), session.getStudent().getEmail(),
                UserRole.STUDENT, message, LocalDateTime.now()));
    }

    public void notifySessionCancelled(TutoringSession session, String cancelledByName) {
        String studentEmail = session.getStudent().getEmail();
        String subject = "Session Cancelled - Tutoring Session #" + session.getId();
        String message = "Your tutoring session with " + session.getTutor().getName()
                + " on " + session.getDate()
                + " from " + session.getStartTime()
                + " to " + session.getEndTime()
                + " has been cancelled by " + cancelledByName + ".";
        String htmlBody = "<h2>Session Cancelled</h2>"
                + "<p>Dear <b>" + session.getStudent().getName() + "</b>,</p>"
                + "<p>Unfortunately, your tutoring session has been cancelled. Here are the details:</p>"
                + "<table style='border-collapse:collapse;width:400px;'>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Session ID</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getId() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Tutor</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getTutor().getName() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Date</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getDate() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Start Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getStartTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>End Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getEndTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Cancelled By</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + cancelledByName + "</td></tr>"
                + "</table>"
                + "<p>We apologize for any inconvenience. Please book another available slot.</p>";
        sendEmail(studentEmail, subject, htmlBody);
        notificationRecordRepository.save(new NotificationRecord(
                session, session.getStudent().getName(), session.getStudent().getEmail(),
                UserRole.STUDENT, message, LocalDateTime.now()));
    }

    public void notifySessionCancelledWithReason(TutoringSession session, String cancelledByName, String reason) {
        String studentEmail = session.getStudent().getEmail();
        String subject = "Session Cancelled - Tutoring Session #" + session.getId();
        String message = "Your tutoring session with " + session.getTutor().getName()
                + " on " + session.getDate()
                + " from " + session.getStartTime()
                + " to " + session.getEndTime()
                + " has been cancelled by " + cancelledByName + ". Reason: " + reason;
        String htmlBody = "<h2>Session Cancelled</h2>"
                + "<p>Dear <b>" + session.getStudent().getName() + "</b>,</p>"
                + "<p>Unfortunately, your tutoring session has been cancelled. Here are the details:</p>"
                + "<table style='border-collapse:collapse;width:400px;'>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Session ID</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getId() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Tutor</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getTutor().getName() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Date</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getDate() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Start Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getStartTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>End Time</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + session.getEndTime() + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Cancelled By</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + cancelledByName + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Reason</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;color:red;'>" + reason + "</td></tr>"
                + "</table>"
                + "<p>We apologize for any inconvenience. Please book another available slot.</p>";
        sendEmail(studentEmail, subject, htmlBody);
        notificationRecordRepository.save(new NotificationRecord(
                session, session.getStudent().getName(), session.getStudent().getEmail(),
                UserRole.STUDENT, message, LocalDateTime.now()));
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom("your-email@gmail.com");
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}