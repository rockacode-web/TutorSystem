package com.example.tutoringsystem.service;

import java.util.List;

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

    private final SessionSlotRepository sessionSlotRepository;
    private final StudentRepository studentRepository;
    private final TutoringSessionRepository tutoringSessionRepository;

    public BookingService(SessionSlotRepository sessionSlotRepository, StudentRepository studentRepository,
            TutoringSessionRepository tutoringSessionRepository) {
        this.sessionSlotRepository = sessionSlotRepository;
        this.studentRepository = studentRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
    }

    public List<SessionSlot> getAvailableSlots() {
        return sessionSlotRepository.findByAvailableTrue();
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

        TutoringSession savedSession = tutoringSessionRepository.save(tutoringSession);
        slot.setAvailable(false);
        sessionSlotRepository.save(slot);

        return savedSession;
    }
}
