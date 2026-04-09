package com.example.tutoringsystem.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tutoringsystem.model.ChatMessage;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.ChatMessageRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;

@Service
public class ChatService {

    private final TutoringSessionRepository tutoringSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(TutoringSessionRepository tutoringSessionRepository,
            ChatMessageRepository chatMessageRepository) {
        this.tutoringSessionRepository = tutoringSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public TutoringSession getStudentChatSession(Long sessionId, Long studentId) {
        TutoringSession session = getSession(sessionId);
        if (!session.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You can only access chat for your own sessions.");
        }
        validateChatAllowed(session);
        return session;
    }

    public TutoringSession getTutorChatSession(Long sessionId, Long tutorId) {
        TutoringSession session = getSession(sessionId);
        if (!session.getTutor().getId().equals(tutorId)) {
            throw new RuntimeException("You can only access chat for sessions assigned to your account.");
        }
        validateChatAllowed(session);
        return session;
    }

    public List<ChatMessage> getMessages(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void sendStudentMessage(Long sessionId, Student student, String message) {
        TutoringSession session = getStudentChatSession(sessionId, student.getId());
        saveMessage(session, student.getName(), student.getEmail(), student.getRole(), message);
    }

    @Transactional
    public void sendTutorMessage(Long sessionId, Tutor tutor, String message) {
        TutoringSession session = getTutorChatSession(sessionId, tutor.getId());
        saveMessage(session, tutor.getName(), tutor.getEmail(), tutor.getRole(), message);
    }

    private TutoringSession getSession(Long sessionId) {
        return tutoringSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Tutoring session not found."));
    }

    private void validateChatAllowed(TutoringSession session) {
        if (session.getStatus() != SessionStatus.BOOKED) {
            throw new RuntimeException("Chat is only available for active booked sessions.");
        }
    }

    private void saveMessage(TutoringSession session,
            String senderName,
            String senderEmail,
            com.example.tutoringsystem.model.UserRole senderRole,
            String message) {
        String trimmedMessage = message == null ? "" : message.trim();
        if (trimmedMessage.isBlank()) {
            throw new RuntimeException("Enter a message before sending.");
        }

        chatMessageRepository.save(new ChatMessage(
                session,
                senderName,
                senderEmail,
                senderRole,
                trimmedMessage,
                LocalDateTime.now()));
    }
}
