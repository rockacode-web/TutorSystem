package com.example.tutoringsystem.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.tutoringsystem.service.PortalIdentityService;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributes {

    private final PortalIdentityService portalIdentityService;

    public GlobalModelAttributes(PortalIdentityService portalIdentityService) {
        this.portalIdentityService = portalIdentityService;
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        return isAuthenticated(authentication) ? authentication.getName() : null;
    }

    @ModelAttribute("isStudent")
    public boolean isStudent(Authentication authentication) {
        return hasRole(authentication, "ROLE_STUDENT");
    }

    @ModelAttribute("isTutor")
    public boolean isTutor(Authentication authentication) {
        return hasRole(authentication, "ROLE_TUTOR");
    }

    @ModelAttribute("currentRoleLabel")
    public String currentRoleLabel(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        return isTutor(authentication) ? "Tutor" : "Student";
    }

    @ModelAttribute("currentDisplayName")
    public String currentDisplayName(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return null;
        }

        String username = authentication.getName();
        if (isTutor(authentication)) {
            return portalIdentityService.getTutorByUsername(username).getName();
        }

        if (isStudent(authentication)) {
            return portalIdentityService.getStudentByUsername(username).getName();
        }

        return username;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return isAuthenticated(authentication)
                && authentication.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
