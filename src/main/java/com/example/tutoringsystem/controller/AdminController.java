package com.example.tutoringsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.tutoringsystem.model.UserRole;
import com.example.tutoringsystem.service.AdminService;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("dashboardReport", adminService.buildReport("overview"));
        model.addAttribute("pendingCount", adminService.getPendingStudentCount());
        return "admin/dashboard";
    }

    @GetMapping("/admin/pending-users")
    public String showPendingUsers(Model model) {
        model.addAttribute("pendingUsers", adminService.getPendingStudents());
        return "admin/pending-users";
    }

    @PostMapping("/admin/users/approve")
    public String approveStudent(@RequestParam Long studentId, RedirectAttributes redirectAttributes) {
        try {
            adminService.approveStudent(studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Student account approved successfully.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/pending-users";
    }

    @PostMapping("/admin/users/reject")
    public String rejectStudent(@RequestParam Long studentId, RedirectAttributes redirectAttributes) {
        try {
            adminService.rejectStudent(studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Student account rejected.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/pending-users";
    }

    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/admin/users/deactivate")
    public String deactivateUser(@AuthenticationPrincipal UserDetails userDetails,
            @RequestParam UserRole role,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.deactivateUser(role, userId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "User account deactivated.");
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/reports")
    public String showReports(@RequestParam(defaultValue = "overview") String type, Model model) {
        model.addAttribute("report", adminService.buildReport(type));
        return "admin/reports";
    }
}
