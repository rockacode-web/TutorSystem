package com.example.tutoringsystem.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.tutoringsystem.model.Admin;
import com.example.tutoringsystem.model.ApprovalStatus;
import com.example.tutoringsystem.model.ChatMessage;
import com.example.tutoringsystem.model.SessionSlot;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.AdminRepository;
import com.example.tutoringsystem.repository.ChatMessageRepository;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;
import com.example.tutoringsystem.service.NotificationService;

@Component
public class DataLoader implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final TutoringSessionRepository tutoringSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public DataLoader(AdminRepository adminRepository,
            StudentRepository studentRepository,
            TutorRepository tutorRepository,
            SessionSlotRepository sessionSlotRepository,
            TutoringSessionRepository tutoringSessionRepository,
            ChatMessageRepository chatMessageRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
        this.sessionSlotRepository = sessionSlotRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() != 0) {
            return;
        }

        adminRepository.save(new Admin(
                DemoAccounts.ADMIN_NAME,
                DemoAccounts.ADMIN_EMAIL,
                passwordEncoder.encode(DemoAccounts.ADMIN_PASSWORD),
                true));

        Student studentOne = studentRepository.save(new Student(
                DemoAccounts.STUDENT_NAME,
                DemoAccounts.STUDENT_EMAIL,
                passwordEncoder.encode(DemoAccounts.STUDENT_PASSWORD),
                DemoAccounts.STUDENT_ID_NUMBER,
                ApprovalStatus.APPROVED,
                true));
        Student pendingStudent = studentRepository.save(new Student(
                DemoAccounts.SECOND_STUDENT_NAME,
                DemoAccounts.SECOND_STUDENT_EMAIL,
                passwordEncoder.encode(DemoAccounts.SECOND_STUDENT_PASSWORD),
                DemoAccounts.SECOND_STUDENT_ID_NUMBER,
                ApprovalStatus.PENDING,
                true));

        Tutor tutorOne = tutorRepository.save(new Tutor(
                DemoAccounts.TUTOR_NAME,
                DemoAccounts.TUTOR_EMAIL,
                passwordEncoder.encode(DemoAccounts.TUTOR_PASSWORD),
                "Physics",
                ApprovalStatus.APPROVED,
                true));
        Tutor tutorTwo = tutorRepository.save(new Tutor(
                DemoAccounts.SECOND_TUTOR_NAME,
                DemoAccounts.SECOND_TUTOR_EMAIL,
                passwordEncoder.encode(DemoAccounts.SECOND_TUTOR_PASSWORD),
                "Mathematics",
                ApprovalStatus.APPROVED,
                true));

        sessionSlotRepository.save(new SessionSlot(
                tutorOne,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                true));

        sessionSlotRepository.save(new SessionSlot(
                tutorOne,
                LocalDate.now().plusDays(2),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                true));

        sessionSlotRepository.save(new SessionSlot(
                tutorTwo,
                LocalDate.now().plusDays(3),
                LocalTime.of(14, 30),
                LocalTime.of(15, 30),
                true));

        SessionSlot bookedSlot = sessionSlotRepository.save(new SessionSlot(
                tutorOne,
                LocalDate.now().plusDays(4),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                false));

        TutoringSession bookedSession = new TutoringSession(
                studentOne,
                tutorOne,
                LocalDate.now().plusDays(4),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                SessionStatus.BOOKED);
        bookedSession.setSlot(bookedSlot);
        bookedSession = tutoringSessionRepository.save(bookedSession);

        SessionSlot cancelledSlot = sessionSlotRepository.save(new SessionSlot(
                tutorOne,
                LocalDate.now().plusDays(6),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                true));

        TutoringSession cancelledSession = new TutoringSession(
                studentOne,
                tutorOne,
                LocalDate.now().plusDays(6),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                SessionStatus.CANCELLED);
        cancelledSession.setSlot(cancelledSlot);
        cancelledSession = tutoringSessionRepository.save(cancelledSession);

        chatMessageRepository.save(new ChatMessage(
                bookedSession,
                studentOne.getName(),
                studentOne.getEmail(),
                studentOne.getRole(),
                "Hello, I would like to focus on Newton's laws during this session.",
                LocalDateTime.now().minusDays(1)));
        chatMessageRepository.save(new ChatMessage(
                bookedSession,
                tutorOne.getName(),
                tutorOne.getEmail(),
                tutorOne.getRole(),
                "Confirmed. Please bring your lab worksheet and any questions you already have.",
                LocalDateTime.now().minusHours(12)));

        notificationService.notifyRegistrationSubmitted(pendingStudent);
        notificationService.notifyBookingConfirmed(bookedSession);
        notificationService.notifySessionCancelled(cancelledSession, tutorOne.getName());
    }
}
