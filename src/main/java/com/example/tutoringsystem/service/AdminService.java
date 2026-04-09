package com.example.tutoringsystem.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tutoringsystem.dto.AdminUserView;
import com.example.tutoringsystem.dto.ReportMetric;
import com.example.tutoringsystem.dto.ReportSummary;
import com.example.tutoringsystem.model.Admin;
import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.UserRole;
import com.example.tutoringsystem.repository.AdminRepository;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class AdminService {

    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final AdminRepository adminRepository;
    private final TutoringSessionRepository tutoringSessionRepository;
    private final SessionSlotRepository sessionSlotRepository;

    public AdminService(StudentRepository studentRepository,
            TutorRepository tutorRepository,
            AdminRepository adminRepository,
            TutoringSessionRepository tutoringSessionRepository,
            SessionSlotRepository sessionSlotRepository) {
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
        this.adminRepository = adminRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.sessionSlotRepository = sessionSlotRepository;
    }

    public List<Student> getPendingStudents() {
        return studentRepository.findByApprovalStatusOrderByNameAsc(ApprovalStatus.PENDING);
    }

    public List<AdminUserView> getAllUsers() {
        List<AdminUserView> users = new ArrayList<>();
        studentRepository.findAllByOrderByNameAsc().forEach(student -> users.add(new AdminUserView(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getRole(),
                student.getApprovalStatus(),
                student.isActive(),
                "Student ID: " + student.getIdNumber())));
        tutorRepository.findAllByOrderByNameAsc().forEach(tutor -> users.add(new AdminUserView(
                tutor.getId(),
                tutor.getName(),
                tutor.getEmail(),
                tutor.getRole(),
                tutor.getApprovalStatus(),
                tutor.isActive(),
                "Subject: " + tutor.getSubject())));
        adminRepository.findAllByOrderByNameAsc().forEach(admin -> users.add(new AdminUserView(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole(),
                admin.getApprovalStatus(),
                admin.isActive(),
                "System administration")));

        users.sort(Comparator.comparing(AdminUserView::getRole).thenComparing(AdminUserView::getName));
        return users;
    }

    public ReportSummary buildReport(String type) {
        String normalizedType = (type == null || type.isBlank()) ? "overview" : type.toLowerCase();
        return switch (normalizedType) {
            case "users" -> new ReportSummary(
                    "users",
                    "User Report",
                    "Overview of registered student, tutor, and administrator accounts.",
                    List.of(
                            new ReportMetric("Total users", String.valueOf(totalUsers())),
                            new ReportMetric("Approved students",
                                    String.valueOf(studentRepository.findAllByOrderByNameAsc().stream()
                                            .filter(student -> student.getApprovalStatus() == ApprovalStatus.APPROVED)
                                            .count())),
                            new ReportMetric("Pending students",
                                    String.valueOf(studentRepository.findByApprovalStatusOrderByNameAsc(ApprovalStatus.PENDING)
                                            .size())),
                            new ReportMetric("Active tutors",
                                    String.valueOf(tutorRepository.findAllByOrderByNameAsc().stream()
                                            .filter(Tutor::isActive)
                                            .count())),
                            new ReportMetric("Administrators", String.valueOf(adminRepository.count()))));
            case "sessions" -> new ReportSummary(
                    "sessions",
                    "Session Report",
                    "Current tutoring session volume by booking status.",
                    List.of(
                            new ReportMetric("Total sessions", String.valueOf(tutoringSessionRepository.count())),
                            new ReportMetric("Booked sessions",
                                    String.valueOf(tutoringSessionRepository.countByStatus(SessionStatus.BOOKED))),
                            new ReportMetric("Cancelled sessions",
                                    String.valueOf(tutoringSessionRepository.countByStatus(SessionStatus.CANCELLED))),
                            new ReportMetric("Completed sessions",
                                    String.valueOf(tutoringSessionRepository.countByStatus(SessionStatus.COMPLETED)))));
            case "availability" -> new ReportSummary(
                    "availability",
                    "Availability Report",
                    "Current slot inventory across the tutoring portal.",
                    List.of(
                            new ReportMetric("Total slots", String.valueOf(sessionSlotRepository.count())),
                            new ReportMetric("Active slots", String.valueOf(sessionSlotRepository.countByAvailableTrue())),
                            new ReportMetric("Booked slots",
                                    String.valueOf(sessionSlotRepository.count() - sessionSlotRepository.countByAvailableTrue()))));
            default -> new ReportSummary(
                    "overview",
                    "Overview Report",
                    "High-level portal metrics for documentation and demonstration.",
                    List.of(
                            new ReportMetric("Total users", String.valueOf(totalUsers())),
                            new ReportMetric("Total sessions", String.valueOf(tutoringSessionRepository.count())),
                            new ReportMetric("Booked vs cancelled",
                                    tutoringSessionRepository.countByStatus(SessionStatus.BOOKED)
                                            + " / "
                                            + tutoringSessionRepository.countByStatus(SessionStatus.CANCELLED)),
                            new ReportMetric("Active slots", String.valueOf(sessionSlotRepository.countByAvailableTrue())),
                            new ReportMetric("Pending approvals",
                                    String.valueOf(studentRepository.findByApprovalStatusOrderByNameAsc(ApprovalStatus.PENDING)
                                            .size()))));
        };
    }

    public long getPendingStudentCount() {
        return studentRepository.findByApprovalStatusOrderByNameAsc(ApprovalStatus.PENDING).size();
    }

    @Transactional
    public void approveStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student account not found."));
        student.setApprovalStatus(ApprovalStatus.APPROVED);
        student.setActive(true);
        studentRepository.save(student);
    }

    @Transactional
    public void rejectStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student account not found."));
        student.setApprovalStatus(ApprovalStatus.REJECTED);
        studentRepository.save(student);
    }

    @Transactional
    public void deactivateUser(UserRole role, Long userId, String currentAdminEmail) {
        switch (role) {
            case STUDENT -> deactivateStudent(userId);
            case TUTOR -> deactivateTutor(userId);
            case ADMIN -> deactivateAdmin(userId, currentAdminEmail);
        }
    }

    private void deactivateStudent(Long userId) {
        Student student = studentRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Student account not found."));
        student.setActive(false);
        studentRepository.save(student);
    }

    private void deactivateTutor(Long userId) {
        Tutor tutor = tutorRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Tutor account not found."));
        tutor.setActive(false);
        tutorRepository.save(tutor);
    }

    private void deactivateAdmin(Long userId, String currentAdminEmail) {
        Admin admin = adminRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Admin account not found."));
        if (admin.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new RuntimeException("You cannot deactivate the administrator account currently in use.");
        }
        admin.setActive(false);
        adminRepository.save(admin);
    }

    private long totalUsers() {
        return studentRepository.count() + tutorRepository.count() + adminRepository.count();
    }
}
