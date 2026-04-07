package com.example.tutoringsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.service.BookingService;
import com.example.tutoringsystem.service.PortalIdentityService;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final PortalIdentityService portalIdentityService;

    public BookingController(BookingService bookingService, PortalIdentityService portalIdentityService) {
        this.bookingService = bookingService;
        this.portalIdentityService = portalIdentityService;
    }

    @GetMapping("/student/book")
    public String showBookingPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("slots", bookingService.getAvailableSlots());
        model.addAttribute("student", portalIdentityService.getStudentByUsername(userDetails.getUsername()));
        return "student/book-session";
    }

    @PostMapping("/student/book")
    public String bookSession(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long slotId,
            RedirectAttributes redirectAttributes) {
        try {
            Long studentId = portalIdentityService.getStudentByUsername(userDetails.getUsername()).getId();
            TutoringSession tutoringSession = bookingService.bookSession(studentId, slotId);
            redirectAttributes.addFlashAttribute("successMessage", "Tutoring session booked successfully.");
            redirectAttributes.addFlashAttribute("sessionId", tutoringSession.getId());
            return "redirect:/student/book/success";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/student/book";
        }
    }

    @GetMapping("/student/book/success")
    public String showBookingSuccessPage() {
        return "student/booking-success";
    }
}
