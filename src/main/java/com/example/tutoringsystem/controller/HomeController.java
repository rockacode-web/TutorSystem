package com.example.tutoringsystem.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        boolean isTutor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TUTOR"));

        if (isTutor) {
            return "redirect:/tutor/schedule";
        }

        return "redirect:/student/book";
    }
}
