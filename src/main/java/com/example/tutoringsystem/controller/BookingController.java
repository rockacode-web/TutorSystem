package com.example.tutoringsystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.service.BookingService;

@Controller
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/student/book")
    public String showBookingPage(Model model) {
        model.addAttribute("slots", bookingService.getAvailableSlots());
        return "student/book-session";
    }

    @PostMapping("/student/book")
    public String bookSession(@RequestParam Long studentId, @RequestParam Long slotId,
            RedirectAttributes redirectAttributes) {
        try {
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
