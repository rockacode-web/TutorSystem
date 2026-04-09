package com.example.tutoringsystem.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.NotificationRecord;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.service.BookingService;
import com.example.tutoringsystem.service.NotificationService;
import com.example.tutoringsystem.service.PortalIdentityService;

@Controller
public class StudentController {

    private final BookingService bookingService;
    private final PortalIdentityService portalIdentityService;
    private final NotificationService notificationService;

    public StudentController(BookingService bookingService,
            PortalIdentityService portalIdentityService,
            NotificationService notificationService) {
        this.bookingService = bookingService;
        this.portalIdentityService = portalIdentityService;
        this.notificationService = notificationService;
    }

    @GetMapping("/student/schedule")
    public String showStudentSchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Student student = portalIdentityService.getStudentByUsername(userDetails.getUsername());
        List<TutoringSession> sessions = bookingService.getStudentSessions(student.getId());
        List<TutoringSession> upcomingSessions = sessions.stream()
                .filter(session -> session.getStatus() != SessionStatus.CANCELLED)
                .toList();
        List<TutoringSession> cancelledSessions = sessions.stream()
                .filter(session -> session.getStatus() == SessionStatus.CANCELLED)
                .toList();
        List<NotificationRecord> notifications = notificationService.getRecentNotifications(student.getEmail());

        model.addAttribute("student", student);
        model.addAttribute("upcomingSessions", upcomingSessions);
        model.addAttribute("cancelledSessions", cancelledSessions);
        model.addAttribute("notifications", notifications);
        return "student/schedule";
    }

    @PostMapping("/student/session/cancel")
    public String cancelStudentSession(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId,
            RedirectAttributes redirectAttributes) {
        try {
            Student student = portalIdentityService.getStudentByUsername(userDetails.getUsername());
            bookingService.cancelStudentSession(student.getId(), sessionId);
            redirectAttributes.addFlashAttribute("successMessage", "Tutoring session cancelled successfully.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/student/schedule";
    }
}
