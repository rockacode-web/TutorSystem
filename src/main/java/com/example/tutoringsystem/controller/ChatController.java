package com.example.tutoringsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.service.ChatService;
import com.example.tutoringsystem.service.PortalIdentityService;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final PortalIdentityService portalIdentityService;

    public ChatController(ChatService chatService, PortalIdentityService portalIdentityService) {
        this.chatService = chatService;
        this.portalIdentityService = portalIdentityService;
    }

    @GetMapping("/student/chat/{sessionId}")
    public String showStudentChat(@PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Student student = portalIdentityService.getStudentByUsername(userDetails.getUsername());
            TutoringSession session = chatService.getStudentChatSession(sessionId, student.getId());
            populateChatModel(model, session, "/student/chat/" + sessionId, "/student/schedule", "Student Session Chat");
            return "chat/session-chat";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/student/schedule";
        }
    }

    @PostMapping("/student/chat/{sessionId}")
    public String sendStudentMessage(@PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        try {
            Student student = portalIdentityService.getStudentByUsername(userDetails.getUsername());
            chatService.sendStudentMessage(sessionId, student, message);
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/student/chat/" + sessionId;
    }

    @GetMapping("/tutor/chat/{sessionId}")
    public String showTutorChat(@PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Tutor tutor = portalIdentityService.getTutorByUsername(userDetails.getUsername());
            TutoringSession session = chatService.getTutorChatSession(sessionId, tutor.getId());
            populateChatModel(model, session, "/tutor/chat/" + sessionId, "/tutor/schedule", "Tutor Session Chat");
            return "chat/session-chat";
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            return "redirect:/tutor/schedule";
        }
    }

    @PostMapping("/tutor/chat/{sessionId}")
    public String sendTutorMessage(@PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        try {
            Tutor tutor = portalIdentityService.getTutorByUsername(userDetails.getUsername());
            chatService.sendTutorMessage(sessionId, tutor, message);
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/tutor/chat/" + sessionId;
    }

    private void populateChatModel(Model model,
            TutoringSession session,
            String postUrl,
            String returnUrl,
            String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("chatSession", session);
        model.addAttribute("messages", chatService.getMessages(session.getId()));
        model.addAttribute("postUrl", postUrl);
        model.addAttribute("returnUrl", returnUrl);
    }
}
