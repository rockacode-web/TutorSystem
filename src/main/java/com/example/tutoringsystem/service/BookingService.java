package com.example.tutoringsystem.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tutoringsystem.model.SessionSlot;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;

@Service
public class BookingService {

    private final SessionSlotRepository sessionSlotRepository;
    private final StudentRepository studentRepository;
    private final TutoringSessionRepository tutoringSessionRepository;
    private final NotificationService emailService;

    public BookingService(SessionSlotRepository sessionSlotRepository,
            StudentRepository studentRepository,
            TutoringSessionRepository tutoringSessionRepository,
            NotificationService emailService) {
        this.sessionSlotRepository = sessionSlotRepository;
        this.studentRepository = studentRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.emailService = emailService;
    }

    public List<SessionSlot> getAvailableSlots() {
        return sessionSlotRepository.findByAvailableTrueOrderByDateAscStartTimeAsc();
    }

    public List<TutoringSession> getStudentSessions(Long studentId) {
        return tutoringSessionRepository.findByStudentIdOrderByDateAscStartTimeAsc(studentId);
    }

    @Transactional
    public TutoringSession bookSession(Long studentId, Long slotId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        SessionSlot slot = sessionSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Session slot not found with id: " + slotId));

        if (!slot.isAvailable()) {
            throw new RuntimeException("That tutoring slot is no longer available. Please choose another session.");
        }

        validateStudentConflict(studentId, slot.getDate(), slot.getStartTime(), slot.getEndTime());

        TutoringSession tutoringSession = new TutoringSession(
                student,
                slot.getTutor(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                SessionStatus.BOOKED);
        tutoringSession.setSlot(slot);

        TutoringSession savedSession = tutoringSessionRepository.save(tutoringSession);

        slot.setAvailable(false);
        sessionSlotRepository.save(slot);
        notificationService.notifyBookingConfirmed(savedSession);

        return savedSession;
    }

    @Transactional
    public TutoringSession cancelStudentSession(Long studentId, Long sessionId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));
        TutoringSession session = tutoringSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Tutoring session not found with id: " + sessionId));

        if (!session.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You can only cancel sessions booked on your own account.");
        }

        // Send booking confirmation email
        emailService.sendBookingConfirmation(savedSession);

        return savedSession;
    }
}
