package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.ExamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamSeatingRepository seatingRepository;
    private final AcademicTermRepository termRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final CourseSectionRepository sectionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    public ExamServiceImpl(ExamRepository examRepository,
                           ExamSeatingRepository seatingRepository,
                           AcademicTermRepository termRepository,
                           CourseRepository courseRepository,
                           RoomRepository roomRepository,
                           CourseSectionRepository sectionRepository,
                           EnrollmentRepository enrollmentRepository,
                           StudentRepository studentRepository,
                           FacultyRepository facultyRepository) {
        this.examRepository = examRepository;
        this.seatingRepository = seatingRepository;
        this.termRepository = termRepository;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.sectionRepository = sectionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    // ================================================================
    // ADMIN: Exam CRUD
    // ================================================================
    @Override
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        // Validate time order
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time.");
        }

        // Validate not past
        if (request.getExamDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot schedule exam in the past.");
        }

        // Fetch entities
        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Term not found: " + request.getTermId()));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));

        // Room double-booking check
        List<Exam> overlapping = examRepository.findOverlappingExams(
                room.getId(), request.getExamDate(), request.getStartTime(), request.getEndTime());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Room " + room.getRoomNumber()
                    + " is already booked for an overlapping exam.");
        }

        Exam exam = new Exam();
        exam.setTerm(term);
        exam.setCourse(course);
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setRoom(room);

        Exam saved = examRepository.save(exam);
        return mapExamToResponse(saved);
    }

    @Override
    public List<ExamResponse> getAllExams() {
        return examRepository.findAll().stream()
                .map(this::mapExamToResponse).collect(Collectors.toList());
    }

    @Override
    public ExamResponse getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));
        return mapExamToResponse(exam);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time.");
        }

        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Overlap check (excluding current exam)
        List<Exam> overlapping = examRepository.findOverlappingExams(
                        room.getId(), request.getExamDate(), request.getStartTime(), request.getEndTime())
                .stream().filter(e -> !e.getId().equals(id)).collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Room is already booked for an overlapping exam.");
        }

        exam.setTerm(term);
        exam.setCourse(course);
        exam.setExamType(request.getExamType());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setRoom(room);

        return mapExamToResponse(examRepository.save(exam));
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + id));

        // Cannot delete if seats allocated
        if (seatingRepository.existsByExamId(id)) {
            throw new BadRequestException(
                    "Cannot delete exam: seating already allocated. Clear seating first.");
        }

        examRepository.delete(exam);
    }

    @Override
    public List<ExamResponse> getExamsByTerm(Long termId) {
        if (!termRepository.existsById(termId)) {
            throw new ResourceNotFoundException("Term not found: " + termId);
        }
        return examRepository.findByTermId(termId).stream()
                .map(this::mapExamToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExamResponse> getExamsByCourse(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        return examRepository.findByCourseId(courseId).stream()
                .map(this::mapExamToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN: Auto Seat Allocation
    // ================================================================
    @Override
    @Transactional
    public List<ExamSeatingResponse> allocateSeats(Long examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found: " + examId));

        // Already allocated?
        if (seatingRepository.existsByExamId(examId)) {
            throw new BadRequestException(
                    "Seats already allocated. Clear existing seating first.");
        }

        // Get all sections of this course in this term
        List<CourseSection> sections = sectionRepository.findByCourseId(exam.getCourse().getId())
                .stream()
                .filter(s -> s.getTerm().getId().equals(exam.getTerm().getId()))
                .collect(Collectors.toList());

        if (sections.isEmpty()) {
            throw new BadRequestException("No sections found for this course in the current term.");
        }

        // Get all actively enrolled students across those sections
        List<Student> students = new ArrayList<>();
        for (CourseSection s : sections) {
            enrollmentRepository.findBySectionId(s.getId()).stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                    .forEach(e -> students.add(e.getStudent()));
        }

        if (students.isEmpty()) {
            throw new BadRequestException("No enrolled students found for this exam.");
        }

        // Room capacity check
        if (students.size() > exam.getRoom().getCapacity()) {
            throw new BadRequestException(
                    "Room capacity (" + exam.getRoom().getCapacity()
                            + ") is smaller than number of students (" + students.size() + ").");
        }

        // Simple seat allocation: A1, A2, A3, ..., B1, B2, ...
        List<ExamSeating> allocations = new ArrayList<>();
        int seatIndex = 0;
        for (Student student : students) {
            char row = (char) ('A' + (seatIndex / 10));
            int col = (seatIndex % 10) + 1;
            String seatNumber = String.valueOf(row) + col;  // A1, A2, ..., A10, B1, ...

            ExamSeating seating = new ExamSeating();
            seating.setExam(exam);
            seating.setStudent(student);
            seating.setSeatNumber(seatNumber);

            allocations.add(seatingRepository.save(seating));
            seatIndex++;
        }

        return allocations.stream().map(this::mapSeatingToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExamSeatingResponse> getExamSeating(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found: " + examId);
        }
        return seatingRepository.findByExamIdOrderBySeatNumberAsc(examId).stream()
                .map(this::mapSeatingToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void clearSeating(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found: " + examId);
        }
        seatingRepository.deleteAllByExamId(examId);
    }

    // ================================================================
    // ADMIN: Assign invigilator to a whole exam
    // ================================================================
    @Override
    @Transactional
    public List<ExamSeatingResponse> assignInvigilator(Long examId, Long facultyId) {
        if (!examRepository.existsById(examId)) {
            throw new ResourceNotFoundException("Exam not found: " + examId);
        }
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        List<ExamSeating> seatings = seatingRepository.findByExamId(examId);
        if (seatings.isEmpty()) {
            throw new BadRequestException("No seating allocated yet. Allocate seats first.");
        }

        for (ExamSeating s : seatings) {
            s.setInvigilator(faculty);
            seatingRepository.save(s);
        }

        return seatings.stream().map(this::mapSeatingToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: Upcoming exams
    // ================================================================
    @Override
    public List<ExamResponse> getMyUpcomingExams(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);

        // Get all my enrollments
        List<Long> enrolledCourseIds = enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(e -> e.getSection().getCourse().getId())
                .distinct()
                .collect(Collectors.toList());

        return examRepository.findByExamDateAfterOrderByExamDateAscStartTimeAsc(LocalDate.now())
                .stream()
                .filter(ex -> enrolledCourseIds.contains(ex.getCourse().getId()))
                .map(this::mapExamToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExamSeatingResponse> getMySeating(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);

        return seatingRepository.findAll().stream()
                .filter(s -> s.getStudent().getId().equals(student.getId()))
                .map(this::mapSeatingToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExamSeatingResponse getMySeatForExam(String studentEmail, Long examId) {
        Student student = getStudentByEmail(studentEmail);

        ExamSeating seating = seatingRepository.findByExamIdAndStudentId(examId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No seat allocated for you in exam: " + examId));

        return mapSeatingToResponse(seating);
    }

    // ================================================================
    // FACULTY: Invigilation
    // ================================================================
    @Override
    public List<ExamSeatingResponse> getMyInvigilations(String facultyEmail) {
        Faculty faculty = facultyRepository.findByUserEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

        return seatingRepository.findByInvigilatorId(faculty.getId()).stream()
                .map(this::mapSeatingToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExamSeatingResponse> getStudentsForInvigilatedExam(String facultyEmail, Long examId) {
        Faculty faculty = facultyRepository.findByUserEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));

        return seatingRepository.findByExamId(examId).stream()
                .filter(s -> s.getInvigilator() != null
                        && s.getInvigilator().getId().equals(faculty.getId()))
                .map(this::mapSeatingToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Student getStudentByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
    }

    private ExamResponse mapExamToResponse(Exam exam) {
        long allocated = seatingRepository.countByExamId(exam.getId());
        Room room = exam.getRoom();

        return ExamResponse.builder()
                .id(exam.getId())
                .examType(exam.getExamType())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .termId(exam.getTerm().getId())
                .termName(exam.getTerm().getName())
                .termCode(exam.getTerm().getTermCode())
                .courseId(exam.getCourse().getId())
                .courseCode(exam.getCourse().getCode())
                .courseTitle(exam.getCourse().getTitle())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .buildingName(room.getBuilding() != null ? room.getBuilding().getName() : null)
                .roomCapacity(room.getCapacity())
                .allocatedSeats(allocated)
                .availableSeats(room.getCapacity() - allocated)
                .build();
    }

    private ExamSeatingResponse mapSeatingToResponse(ExamSeating s) {
        Exam exam = s.getExam();
        Room room = exam.getRoom();
        Student student = s.getStudent();
        Faculty invigilator = s.getInvigilator();

        return ExamSeatingResponse.builder()
                .id(s.getId())
                .seatNumber(s.getSeatNumber())
                .examId(exam.getId())
                .examType(exam.getExamType().name())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .courseCode(exam.getCourse().getCode())
                .courseTitle(exam.getCourse().getTitle())
                .roomNumber(room.getRoomNumber())
                .buildingName(room.getBuilding() != null ? room.getBuilding().getName() : null)
                .studentId(student.getId())
                .studentRollNumber(student.getRollNumber())
                .studentFullName(student.getUser().getFirstName() + " "
                        + student.getUser().getLastName())
                .invigilatorId(invigilator != null ? invigilator.getId() : null)
                .invigilatorName(invigilator != null
                        ? invigilator.getUser().getFirstName() + " "
                          + invigilator.getUser().getLastName()
                        : null)
                .build();
    }
}