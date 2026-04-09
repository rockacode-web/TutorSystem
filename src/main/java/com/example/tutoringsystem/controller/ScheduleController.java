package com.example.tutoringsystem.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.service.PortalIdentityService;
import com.example.tutoringsystem.service.ScheduleService;

@Controller
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleService scheduleService;
    private final PortalIdentityService portalIdentityService;

    public ScheduleController(ScheduleService scheduleService, PortalIdentityService portalIdentityService) {
        this.scheduleService = scheduleService;
        this.portalIdentityService = portalIdentityService;
    }

    @GetMapping("/tutor/schedule")
    public String showTutorSchedule(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Tutor tutor = portalIdentityService.getTutorByUsername(userDetails.getUsername());
        List<TutoringSession> sessions = scheduleService.getSessionsByTutor(tutor.getId());
        model.addAttribute("sessions", sessions);
        model.addAttribute("slots", scheduleService.getSlotsByTutor(tutor.getId()));
        model.addAttribute("tutor", tutor);
        return "tutor/manage-schedule";
    }

    @PostMapping("/tutor/slot/create")
    public String createSessionSlot(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            RedirectAttributes redirectAttributes) {
        Tutor tutor = portalIdentityService.getTutorByUsername(userDetails.getUsername());
        Long tutorId = tutor.getId();
        logger.info("Schedule slot creation requested: tutorId={}, date={}, startTime={}, endTime={}",
                tutorId, date, startTime, endTime);

        try {
            LocalDate parsedDate = LocalDate.parse(date);
            LocalTime parsedStartTime = LocalTime.parse(startTime);
            LocalTime parsedEndTime = LocalTime.parse(endTime);

            scheduleService.createSessionSlot(tutorId, parsedDate, parsedStartTime, parsedEndTime);
            logger.info("Schedule slot creation succeeded: tutorId={}", tutorId);
            redirectAttributes.addFlashAttribute("successMessage", "New available slot added successfully.");
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to add slot: invalid date or time value.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to add slot: " + exception.getMessage());
        }
        return "redirect:/tutor/schedule";
    }

    @PostMapping("/tutor/schedule/update")
    public String updateSession(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long sessionId,
            @RequestParam(required = false) String newDate,
            @RequestParam(required = false) String newStartTime,
            @RequestParam(required = false) String newEndTime,
            RedirectAttributes redirectAttributes) {
        Tutor tutor = portalIdentityService.getTutorByUsername(userDetails.getUsername());
        Long tutorId = tutor.getId();
        logger.info("Schedule update requested: sessionId={}, tutorId={}, newDate={}, newStartTime={}, newEndTime={}",
                sessionId, tutorId, newDate, newStartTime, newEndTime);

        if (newDate == null || newDate.isBlank() || newStartTime == null || newStartTime.isBlank()
                || newEndTime == null || newEndTime.isBlank()) {
            String message = "Missing required date or time values for session update.";
            logger.warn("Schedule update failed: sessionId={}, tutorId={}, reason={}", sessionId, tutorId, message);
            redirectAttributes.addFlashAttribute("errorMessage", message);
            return "redirect:/tutor/schedule";
        }

        try {
            LocalDate parsedDate = LocalDate.parse(newDate);
            LocalTime parsedStartTime = LocalTime.parse(newStartTime);
            LocalTime parsedEndTime = LocalTime.parse(newEndTime);

            scheduleService.updateSession(sessionId, tutorId, parsedDate, parsedStartTime, parsedEndTime);
            logger.info("Schedule update succeeded: sessionId={}, tutorId={}", sessionId, tutorId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tutoring session updated successfully for session id " + sessionId + ".");
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to update session id " + sessionId + ": invalid date or time value.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to update session id " + sessionId + ": " + exception.getMessage());
        }
        return "redirect:/tutor/schedule";
    }

    @PostMapping("/tutor/schedule/cancel")
    public String cancelSession(@RequestParam Long sessionId,
            @RequestParam Long tutorId,
            @RequestParam String cancelReason,
            @RequestParam(required = false) String otherReason,
            RedirectAttributes redirectAttributes) {
        logger.info("Schedule cancel requested: sessionId={}, tutorId={}, cancelReason={}",
                sessionId, tutorId, cancelReason);

        // Reason for cancellation
        String finalReason;
        if ("Other".equals(cancelReason)) {
            if (otherReason == null || otherReason.isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Please provide a reason when selecting 'Other'.");
                return "redirect:/tutor/schedule?tutorId=" + tutorId;
            }
            finalReason = otherReason;
        } else {
            finalReason = "No longer available";
        }

        try {
            scheduleService.cancelSession(sessionId, finalReason);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tutoring session cancelled successfully for session id " + sessionId + ".");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to cancel session id " + sessionId + ": " + exception.getMessage());
        }
        return "redirect:/tutor/schedule";
    }
}