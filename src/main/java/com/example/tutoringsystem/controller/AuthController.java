package com.example.tutoringsystem.controller;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.dto.StudentRegistrationForm;
import com.example.tutoringsystem.service.RegistrationService;

@Controller
public class AuthController {

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationPage(Authentication authentication, Model model) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new StudentRegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerStudent(@Valid @ModelAttribute("registrationForm") StudentRegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the highlighted registration errors.");
            return "register";
        }

        try {
            registrationService.registerStudent(registrationForm);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration submitted for approval. You can sign in after an administrator approves your account.");
            return "redirect:/login";
        } catch (RuntimeException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "register";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
