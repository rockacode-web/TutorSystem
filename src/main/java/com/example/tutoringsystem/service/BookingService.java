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
    private final NotificationService notificationService;

    public BookingService(SessionSlotRepository sessionSlotRepository, StudentRepository studentRepository,
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

        if (session.getStatus() != SessionStatus.BOOKED) {
            throw new RuntimeException("Only active booked sessions can be cancelled.");
        }

        session.setStatus(SessionStatus.CANCELLED);
        reopenLinkedSlot(session);
        TutoringSession savedSession = tutoringSessionRepository.save(session);
        notificationService.notifySessionCancelled(savedSession, student.getName());
        return savedSession;
    }

    private void validateStudentConflict(Long studentId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        boolean conflictExists = tutoringSessionRepository.findByStudentIdOrderByDateAscStartTimeAsc(studentId).stream()
                .filter(session -> session.getStatus() == SessionStatus.BOOKED)
                .filter(session -> session.getDate().equals(date))
                .anyMatch(session -> overlaps(startTime, endTime, session.getStartTime(), session.getEndTime()));

        if (conflictExists) {
            throw new RuntimeException("You already have a booked session that overlaps with that time.");
        }
    }

    private void reopenLinkedSlot(TutoringSession session) {
        if (session.getSlot() != null) {
            session.getSlot().setAvailable(true);
            sessionSlotRepository.save(session.getSlot());
        }
    }

    private boolean overlaps(LocalTime startTimeA, LocalTime endTimeA, LocalTime startTimeB, LocalTime endTimeB) {
        return startTimeA.isBefore(endTimeB) && endTimeA.isAfter(startTimeB);
    }
}
