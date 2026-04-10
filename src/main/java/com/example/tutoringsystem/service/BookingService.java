package com.example.tutoringsystem.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.SessionSlot;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final SessionSlotRepository sessionSlotRepository;
    private final StudentRepository studentRepository;
    private final TutoringSessionRepository tutoringSessionRepository;
    private final NotificationService notificationService;

    public BookingService(SessionSlotRepository sessionSlotRepository,
            StudentRepository studentRepository,
            TutoringSessionRepository tutoringSessionRepository,
            NotificationService notificationService) {
        this.sessionSlotRepository = sessionSlotRepository;
        this.studentRepository = studentRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.notificationService = notificationService;
    }

    public List<SessionSlot> getAvailableSlots() {
        return sessionSlotRepository.findByAvailableTrueOrderByDateAscStartTimeAsc();
    }

    public List<TutoringSession> getStudentSessions(Long studentId) {
        return tutoringSessionRepository.findByStudentId(studentId);
    }

    public TutoringSession bookSession(Long studentId, Long slotId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + studentId));

        SessionSlot slot = sessionSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Session slot not found with id: " + slotId));

        if (!slot.isAvailable()) {
            throw new RuntimeException("Session slot is not available for booking: " + slotId);
        }

        TutoringSession tutoringSession = new TutoringSession(
                student,
                slot.getTutor(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                SessionStatus.BOOKED);

        // Link the session to the slot so we can find it again on cancellation
        tutoringSession.setSlot(slot);

        TutoringSession savedSession = tutoringSessionRepository.save(tutoringSession);

        slot.setAvailable(false);
        sessionSlotRepository.save(slot);

        notificationService.notifyBookingConfirmed(savedSession);

        return savedSession;
    }

    public TutoringSession cancelStudentSession(Long studentId, Long sessionId) {
        logger.info("cancelStudentSession entered: studentId={}, sessionId={}", studentId, sessionId);

        TutoringSession session = tutoringSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found with id: " + sessionId));

        if (!session.getStudent().getId().equals(studentId)) {
            logger.warn("cancelStudentSession blocked: sessionId={} does not belong to studentId={}",
                    sessionId, studentId);
            throw new RuntimeException("You are not authorized to cancel this session.");
        }

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("Session is already cancelled.");
        }

        // Mark session as cancelled — record is kept
        session.setStatus(SessionStatus.CANCELLED);
        TutoringSession savedSession = tutoringSessionRepository.save(session);
        logger.info("cancelStudentSession status set to CANCELLED: sessionId={}", sessionId);

        // Re-open the slot so it appears on the booking screen again
        SessionSlot slot = session.getSlot();
        if (slot != null) {
            slot.setAvailable(true);
            sessionSlotRepository.save(slot);
            logger.info("cancelStudentSession slot re-opened: slotId={}", slot.getId());
        } else {
            logger.warn("cancelStudentSession no linked slot found for sessionId={}", sessionId);
        }

        notificationService.notifySessionCancelled(savedSession, session.getStudent().getName());

        return savedSession;
    }
}