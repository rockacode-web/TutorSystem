package com.example.tutoringsystem.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.tutoringsystem.model.SessionSlot;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;
import com.example.tutoringsystem.repository.TutorRepository;

@Service
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final TutoringSessionRepository tutoringSessionRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final TutorRepository tutorRepository;

    public ScheduleService(TutoringSessionRepository tutoringSessionRepository,
            SessionSlotRepository sessionSlotRepository,
            TutorRepository tutorRepository) {
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.sessionSlotRepository = sessionSlotRepository;
        this.tutorRepository = tutorRepository;
    }

    public List<TutoringSession> getSessionsByTutor(Long tutorId) {
        return tutoringSessionRepository.findByTutorId(tutorId);
    }

    public SessionSlot createSessionSlot(Long tutorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        logger.info("createSessionSlot entered: tutorId={}, date={}, startTime={}, endTime={}",
                tutorId, date, startTime, endTime);

        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> {
                    logger.warn("createSessionSlot tutor not found: tutorId={}", tutorId);
                    return new RuntimeException("Tutor not found with id: " + tutorId);
                });

        SessionSlot sessionSlot = new SessionSlot();
        sessionSlot.setTutor(tutor);
        sessionSlot.setDate(date);
        sessionSlot.setStartTime(startTime);
        sessionSlot.setEndTime(endTime);
        sessionSlot.setAvailable(true);

        SessionSlot savedSlot = sessionSlotRepository.save(sessionSlot);
        logger.info("createSessionSlot save completed: slotId={}, tutorId={}", savedSlot.getId(), tutorId);
        return savedSlot;
    }

    // MVP state rule: BOOKED -> CANCELLED is allowed, and cancelled sessions remain visible for history.
    public TutoringSession cancelSession(Long sessionId, Long tutorId) {
        logger.info("cancelSession entered: sessionId={}, tutorId={}", sessionId, tutorId);
        TutoringSession tutoringSession = getTutorSession(sessionId, tutorId, "cancelSession");

        logger.info("cancelSession session found: sessionId={}, oldStatus={}", sessionId, tutoringSession.getStatus());
        tutoringSession.setStatus(SessionStatus.CANCELLED);
        logger.info("cancelSession status updated: sessionId={}, newStatus={}", sessionId, tutoringSession.getStatus());
        TutoringSession savedSession = tutoringSessionRepository.save(tutoringSession);
        logger.info("cancelSession save completed: sessionId={}", sessionId);
        return savedSession;
    }

    // MVP state rule: CANCELLED sessions cannot have date/time updated; a future flow can handle reschedule/reactivate.
    public TutoringSession updateSession(Long sessionId, Long tutorId, LocalDate newDate, LocalTime newStartTime,
            LocalTime newEndTime) {
        logger.info("updateSession entered: sessionId={}, tutorId={}", sessionId, tutorId);
        TutoringSession tutoringSession = getTutorSession(sessionId, tutorId, "updateSession");

        logger.info("updateSession session found: sessionId={}, oldDate={}, oldStartTime={}, oldEndTime={}",
                sessionId, tutoringSession.getDate(), tutoringSession.getStartTime(), tutoringSession.getEndTime());

        if (tutoringSession.getStatus() == SessionStatus.CANCELLED) {
            logger.warn("updateSession blocked for cancelled session: sessionId={}", sessionId);
            throw new RuntimeException("Cancelled sessions cannot be updated.");
        }

        logger.info("updateSession new values: sessionId={}, newDate={}, newStartTime={}, newEndTime={}",
                sessionId, newDate, newStartTime, newEndTime);
        tutoringSession.setDate(newDate);
        tutoringSession.setStartTime(newStartTime);
        tutoringSession.setEndTime(newEndTime);

        TutoringSession savedSession = tutoringSessionRepository.save(tutoringSession);
        logger.info("updateSession save completed: sessionId={}", sessionId);
        return savedSession;
    }

    private TutoringSession getTutorSession(Long sessionId, Long tutorId, String operation) {
        TutoringSession tutoringSession = tutoringSessionRepository.findById(sessionId)
                .orElseThrow(() -> {
                    logger.warn("{} session not found: sessionId={}", operation, sessionId);
                    return new RuntimeException("Tutoring session not found with id: " + sessionId);
                });

        if (!tutoringSession.getTutor().getId().equals(tutorId)) {
            logger.warn("{} denied: sessionId={}, tutorId={}", operation, sessionId, tutorId);
            throw new RuntimeException("You can only manage sessions assigned to your account.");
        }

        return tutoringSession;
    }
}
