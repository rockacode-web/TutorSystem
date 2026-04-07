# Tutoring System Project Activity

## Project Title
Tutoring System Application

## Project Overview
This project is a web-based tutoring management system built with Spring Boot. It allows tutors to create available time slots, students to book those slots, and tutors to manage booked sessions. The system uses a layered architecture with models, repositories, services, controllers, and Thymeleaf views.

## Learning Objectives
By completing this activity, students should be able to:
- explain the purpose of each class in the project
- identify how classes connect across the MVC architecture
- describe entity relationships in a database-driven application
- trace data flow from the user interface to the database
- propose improvements to strengthen the system
- recognize the frameworks required to complete and extend the project

## Student Task
You are part of a development team completing this tutoring system. Your job is to study the current project, document the design, explain how the classes work together, and prepare recommendations for completing the final version.

## Activity Instructions
1. Examine the project structure and identify the five major layers:
   - model
   - repository
   - service
   - controller
   - view
2. For each class, record:
   - its purpose
   - its attributes
   - its methods
   - the other classes it depends on
   - its role in the system workflow
3. Draw a class relationship diagram showing how the entities connect.
4. Trace these two workflows:
   - tutor creates an available session slot
   - student books an available session
5. Explain how data moves from:
   - HTML form
   - controller
   - service
   - repository
   - database
6. Recommend at least three improvements that would make the system more complete.

## Deliverables
Students must submit:
- a class analysis table
- a UML-style class relationship diagram
- a workflow explanation for booking and scheduling
- a list of suggested improvements
- a short reflection on the frameworks used in the project

## Detailed Class Guide

### 1. Student
File: `src/main/java/com/example/tutoringsystem/model/Student.java`

**Purpose**  
Represents a student who books tutoring sessions.

**Current Attributes**
- `id: Long`  
  Unique identifier for each student.
- `name: String`  
  Stores the student's full name.
- `email: String`  
  Stores the student's email address.

**Connections**
- A `Student` is linked to many `TutoringSession` records.
- It is used in `BookingService` when a booking is made.
- It is stored through `StudentRepository`.

**Possible Additional Attributes**
- `phoneNumber: String`
- `gradeLevel: String`
- `preferredSubject: String`
- `registeredAt: LocalDateTime`

### 2. Tutor
File: `src/main/java/com/example/tutoringsystem/model/Tutor.java`

**Purpose**  
Represents a tutor offering tutoring services.

**Current Attributes**
- `id: Long`  
  Unique tutor identifier.
- `name: String`  
  Tutor's full name.
- `subject: String`  
  Subject taught by the tutor.

**Connections**
- A `Tutor` can have many `SessionSlot` objects.
- A `Tutor` can also have many `TutoringSession` records.
- Used by `ScheduleService` and stored through `TutorRepository`.

**Possible Additional Attributes**
- `email: String`
- `phoneNumber: String`
- `qualification: String`
- `hourlyRate: BigDecimal`
- `bio: String`

### 3. SessionSlot
File: `src/main/java/com/example/tutoringsystem/model/SessionSlot.java`

**Purpose**  
Represents a tutor's available time slot before a student books it.

**Current Attributes**
- `id: Long`  
  Unique slot ID.
- `tutor: Tutor`  
  The tutor who owns the slot.
- `date: LocalDate`  
  Date of the available session.
- `startTime: LocalTime`  
  Start time of the slot.
- `endTime: LocalTime`  
  End time of the slot.
- `available: boolean`  
  Indicates whether the slot can still be booked.

**Connections**
- Many slots can belong to one tutor.
- Used by `BookingService` to find open times.
- Created in `ScheduleService`.
- Stored in `SessionSlotRepository`.

**Possible Additional Attributes**
- `location: String`
- `deliveryMode: String`
- `notes: String`
- `capacity: int`

### 4. TutoringSession
File: `src/main/java/com/example/tutoringsystem/model/TutoringSession.java`

**Purpose**  
Represents a confirmed tutoring appointment between a student and a tutor.

**Current Attributes**
- `id: Long`  
  Unique session ID.
- `student: Student`  
  Student who booked the session.
- `tutor: Tutor`  
  Tutor for the session.
- `date: LocalDate`  
  Session date.
- `startTime: LocalTime`  
  Session start time.
- `endTime: LocalTime`  
  Session end time.
- `status: SessionStatus`  
  Current state of the session.

**Connections**
- Belongs to one student.
- Belongs to one tutor.
- Managed by both `BookingService` and `ScheduleService`.
- Stored through `TutoringSessionRepository`.

**Possible Additional Attributes**
- `topic: String`
- `meetingLink: String`
- `sessionNotes: String`
- `feedback: String`
- `createdAt: LocalDateTime`

### 5. SessionStatus
File: `src/main/java/com/example/tutoringsystem/model/SessionStatus.java`

**Purpose**  
Defines the status of a tutoring session.

**Current Values**
- `BOOKED`
- `CANCELLED`
- `COMPLETED`

**Connections**
- Used inside `TutoringSession`.
- Updated by `ScheduleService`.

**Possible Extensions**
- `PENDING`
- `RESCHEDULED`
- `NO_SHOW`

## Repository Layer

### StudentRepository
File: `src/main/java/com/example/tutoringsystem/repository/StudentRepository.java`  
Handles storage and retrieval of students.

### TutorRepository
File: `src/main/java/com/example/tutoringsystem/repository/TutorRepository.java`  
Handles storage and retrieval of tutors.

### SessionSlotRepository
File: `src/main/java/com/example/tutoringsystem/repository/SessionSlotRepository.java`  
Handles storage and retrieval of available session slots.

Custom method:
- `findByAvailableTrue()`

### TutoringSessionRepository
File: `src/main/java/com/example/tutoringsystem/repository/TutoringSessionRepository.java`  
Handles storage and retrieval of booked sessions.

Custom method:
- `findByTutorId(Long tutorId)`

## Service Layer

### BookingService
File: `src/main/java/com/example/tutoringsystem/service/BookingService.java`

**Purpose**
- gets all available slots
- books a session for a student
- changes a slot from available to unavailable
- creates a `TutoringSession` with status `BOOKED`

**Important Logic**
- finds the student
- finds the selected slot
- verifies that the slot is still available
- creates the tutoring session
- saves the session
- marks the slot unavailable

**Connected Classes**
- `Student`
- `SessionSlot`
- `TutoringSession`
- `StudentRepository`
- `SessionSlotRepository`
- `TutoringSessionRepository`

### ScheduleService
File: `src/main/java/com/example/tutoringsystem/service/ScheduleService.java`

**Purpose**
- gets sessions belonging to a tutor
- creates new tutor availability slots
- updates tutoring session time and date
- cancels sessions

**Important Logic**
- creates `SessionSlot` records for tutors
- changes a session status to `CANCELLED`
- blocks updates to cancelled sessions

**Connected Classes**
- `Tutor`
- `SessionSlot`
- `TutoringSession`
- `SessionStatus`
- `TutorRepository`
- `SessionSlotRepository`
- `TutoringSessionRepository`

## Controller Layer

### HomeController
File: `src/main/java/com/example/tutoringsystem/controller/HomeController.java`  
Displays the home page.

### BookingController
File: `src/main/java/com/example/tutoringsystem/controller/BookingController.java`

**Purpose**
- shows the booking page
- receives booking requests from students
- redirects to the booking success page

**Routes**
- `GET /student/book`
- `POST /student/book`
- `GET /student/book/success`

### ScheduleController
File: `src/main/java/com/example/tutoringsystem/controller/ScheduleController.java`

**Purpose**
- shows tutor schedule page
- receives new slot creation requests
- updates tutoring sessions
- cancels tutoring sessions

**Routes**
- `GET /tutor/schedule`
- `POST /tutor/slot/create`
- `POST /tutor/schedule/update`
- `POST /tutor/schedule/cancel`

## Data Initialization

### DataLoader
File: `src/main/java/com/example/tutoringsystem/config/DataLoader.java`

**Purpose**
- loads sample students, tutors, slots, and sessions into the database when the app starts
- helps demonstrate the system without manual data entry

## How the Classes Connect

### Entity Relationships
- One `Tutor` can have many `SessionSlot` records.
- One `Tutor` can have many `TutoringSession` records.
- One `Student` can have many `TutoringSession` records.
- One `SessionSlot` belongs to one `Tutor`.
- One `TutoringSession` belongs to one `Student`.
- One `TutoringSession` belongs to one `Tutor`.

### Workflow Relationships
1. Tutor creates a slot in the schedule page.
2. `ScheduleController` sends the data to `ScheduleService`.
3. `ScheduleService` creates a `SessionSlot`.
4. `SessionSlotRepository` saves it to the database.

Then:

1. Student opens the booking page.
2. `BookingController` requests available slots from `BookingService`.
3. `BookingService` gets them from `SessionSlotRepository`.
4. Student selects a slot and submits the form.
5. `BookingService` creates a `TutoringSession`.
6. The booked slot is marked unavailable.
7. `TutoringSessionRepository` saves the final booking.

## Simple UML-Style Relationship Summary

```text
Student (1) -------- (many) TutoringSession (many) -------- (1) Tutor

Tutor (1) -------- (many) SessionSlot
```

## System Layer Diagram

```text
HTML/Thymeleaf View
        |
Controller
        |
Service
        |
Repository
        |
H2 Database
```

## Frameworks Needed

### Currently Used
- `Spring Boot`
  Handles application startup and project configuration.
- `Spring MVC`
  Handles routes, controllers, request processing, and form submission.
- `Spring Data JPA`
  Handles entity mapping and repository-based database access.
- `Thymeleaf`
  Builds dynamic server-side HTML pages.
- `Jakarta Validation`
  Supports validation of form and model data.
- `H2 Database`
  Provides a simple in-memory or file-based development database.

### Recommended Additional Frameworks
- `Spring Security`
  For login, user roles, and access control.
- `Lombok`
  To reduce boilerplate code in model classes.
- `JUnit and MockMvc`
  For proper automated testing of services and controllers.
- `Bootstrap`
  For easier interface styling if the UI needs improvement.

## Suggested Improvements for Project Completion
- add validation annotations to model fields such as `@NotBlank` and `@Email`
- prevent overlapping session slots for the same tutor
- allow students to view their booked sessions
- add authentication for tutors and students
- allow sessions to be marked `COMPLETED`
- support tutor subject search and filtering
- add DTOs and better exception handling
- create stronger tests for service and controller behavior

## Assessment Rubric

### Excellent
- clearly explains every class and its role
- identifies all main relationships correctly
- fully explains booking and scheduling workflows
- accurately names the required frameworks
- gives thoughtful improvement suggestions

### Good
- explains most classes correctly
- shows most relationships accurately
- describes workflows with minor gaps
- identifies most frameworks correctly

### Satisfactory
- explains some classes but misses key connections
- gives only partial workflow explanation
- names frameworks with limited detail

### Needs Improvement
- class roles are unclear or incorrect
- relationships are missing
- workflow explanation is incomplete
- frameworks are not identified properly

## Reflection Questions
1. Why is `SessionSlot` separate from `TutoringSession`?
2. Why does the system mark a slot unavailable after booking?
3. What problem does the `SessionStatus` enum solve?
4. Why are services used instead of putting all logic in controllers?
5. What would happen if validation and authentication were missing in a real system?
