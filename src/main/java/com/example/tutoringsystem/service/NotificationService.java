package com.example.tutoringsystem.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.TutoringSession;

import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmation(TutoringSession session) {
        String studentEmail = session.getStudent().getEmail();
        String subject = "Booking Confirmed - Tutoring Session #" + session.getId();
        String body = "<h2>Booking Confirmed!</h2>"
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

        sendEmail(studentEmail, subject, body);
    }

    public void sendCancellationNotice(TutoringSession session, String cancelledBy, String reason) {
        String studentEmail = session.getStudent().getEmail();
        String subject = "Session Cancelled - Tutoring Session #" + session.getId();
        String body = "<h2>Session Cancelled</h2>"
                + "<p>Dear <b>" + session.getStudent().getName() + "</b>,</p>"
                + "<p>Unfortunately, your tutoring session has been cancelled. Here are the details:</p>"
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
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Cancelled By</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;'>" + cancelledBy + "</td></tr>"
                + "<tr><td style='padding:8px;border:1px solid #ddd;'><b>Reason</b></td>"
                + "<td style='padding:8px;border:1px solid #ddd;color:red;'>" + reason + "</td></tr>"
                + "</table>"
                + "<p>We apologize for any inconvenience. Please book another available slot.</p>";

        sendEmail(studentEmail, subject, body);
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