package com.example.tutoringsystem.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.service.ScheduleService;

@Controller
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/tutor/schedule")
    public String showTutorSchedule(@RequestParam Long tutorId, Model model) {
        model.addAttribute("sessions", scheduleService.getSessionsByTutor(tutorId));
        model.addAttribute("tutorId", tutorId);
        return "tutor/manage-schedule";
    }

    @PostMapping("/tutor/slot/create")
    public String createSessionSlot(@RequestParam Long tutorId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            RedirectAttributes redirectAttributes) {
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
        return "redirect:/tutor/schedule?tutorId=" + tutorId;
    }

    @PostMapping("/tutor/schedule/update")
    public String updateSession(@RequestParam Long sessionId,
            @RequestParam(required = false) String newDate,
            @RequestParam(required = false) String newStartTime,
            @RequestParam(required = false) String newEndTime,
            @RequestParam Long tutorId,
            RedirectAttributes redirectAttributes) {

        if (newDate == null || newDate.isBlank() || newStartTime == null || newStartTime.isBlank()
                || newEndTime == null || newEndTime.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Missing required date or time values for session update.");
            return "redirect:/tutor/schedule?tutorId=" + tutorId;
        }

        try {
            LocalDate parsedDate = LocalDate.parse(newDate);
            LocalTime parsedStartTime = LocalTime.parse(newStartTime);
            LocalTime parsedEndTime = LocalTime.parse(newEndTime);

            scheduleService.updateSession(sessionId, parsedDate, parsedStartTime, parsedEndTime);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tutoring session updated successfully for session id " + sessionId + ".");
        } catch (DateTimeParseException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to update session id " + sessionId + ": invalid date or time value.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unable to update session id " + sessionId + ": " + exception.getMessage());
        }
        return "redirect:/tutor/schedule?tutorId=" + tutorId;
    }

    @PostMapping("/tutor/schedule/cancel")
    public String cancelSession(@RequestParam Long sessionId,
            @RequestParam Long tutorId,
            @RequestParam String cancelReason,
            @RequestParam(required = false) String otherReason,
            RedirectAttributes redirectAttributes) {
        logger.info("Schedule cancel requested: sessionId={}, tutorId={}, cancelReason={}",
                sessionId, tutorId, cancelReason);

        // Build the final reason string
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
        return "redirect:/tutor/schedule?tutorId=" + tutorId;
    }
}