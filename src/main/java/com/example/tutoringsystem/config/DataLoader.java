package com.example.tutoringsystem.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.tutoringsystem.model.SessionSlot;
import com.example.tutoringsystem.model.SessionStatus;
import com.example.tutoringsystem.model.Student;
import com.example.tutoringsystem.model.Tutor;
import com.example.tutoringsystem.model.TutoringSession;
import com.example.tutoringsystem.repository.SessionSlotRepository;
import com.example.tutoringsystem.repository.StudentRepository;
import com.example.tutoringsystem.repository.TutorRepository;
import com.example.tutoringsystem.repository.TutoringSessionRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final TutorRepository tutorRepository;
    private final SessionSlotRepository sessionSlotRepository;
    private final TutoringSessionRepository tutoringSessionRepository;

    public DataLoader(StudentRepository studentRepository, TutorRepository tutorRepository,
            SessionSlotRepository sessionSlotRepository, TutoringSessionRepository tutoringSessionRepository) {
        this.studentRepository = studentRepository;
        this.tutorRepository = tutorRepository;
        this.sessionSlotRepository = sessionSlotRepository;
        this.tutoringSessionRepository = tutoringSessionRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() != 0) {
            return;
        }

        Student studentOne = studentRepository.save(new Student("Alicia Brown", "alicia.brown@example.com"));
        Student studentTwo = studentRepository.save(new Student("Daniel Wright", "daniel.wright@example.com"));

        Tutor tutorOne = tutorRepository.save(new Tutor("Maya Thompson", "Mathematics"));
        Tutor tutorTwo = tutorRepository.save(new Tutor("Ethan Clarke", "Physics"));

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

        tutoringSessionRepository.save(new TutoringSession(
                studentTwo,
                tutorOne,
                LocalDate.now().plusDays(4),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                SessionStatus.BOOKED));
    }
}
