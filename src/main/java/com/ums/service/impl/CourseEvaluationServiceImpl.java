package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.CourseEvaluation;
import com.ums.entity.CourseSection;
import com.ums.entity.Enrollment;
import com.ums.entity.Faculty;
import com.ums.entity.Student;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.CourseEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseEvaluationServiceImpl implements CourseEvaluationService {

    private final CourseEvaluationRepository evaluationRepository;
    private final CourseSectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FacultyRepository facultyRepository;

    public CourseEvaluationServiceImpl(CourseEvaluationRepository evaluationRepository,
                                       CourseSectionRepository sectionRepository,
                                       StudentRepository studentRepository,
                                       EnrollmentRepository enrollmentRepository,
                                       FacultyRepository facultyRepository) {
        this.evaluationRepository = evaluationRepository;
        this.sectionRepository = sectionRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.facultyRepository = facultyRepository;
    }

    // ================================================================
    // STUDENT: Submit evaluation
    // ================================================================
    @Override
    @Transactional
    public CourseEvaluationResponse submitEvaluation(String studentEmail,
                                                     Long sectionId,
                                                     CourseEvaluationRequest request) {
        Student student = getStudentByEmail(studentEmail);

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        // 1. Student must be (or have been) enrolled in this section
        boolean wasEnrolled = enrollmentRepository.existsByStudentIdAndSectionId(
                student.getId(), sectionId);
        if (!wasEnrolled) {
            throw new BadRequestException("You were not enrolled in this section.");
        }

        // 2. Cannot evaluate a future term
        if (section.getTerm().getStartDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Cannot evaluate a section from a future term.");
        }

        // 3. One evaluation per student per section
        if (evaluationRepository.existsBySectionIdAndStudentId(sectionId, student.getId())) {
            throw new BadRequestException("You have already submitted an evaluation for this section.");
        }

        // 4. Save (student_id stored in DB but never returned in admin responses)
        CourseEvaluation evaluation = new CourseEvaluation();
        evaluation.setSection(section);
        evaluation.setStudent(student);
        evaluation.setRatingTeaching(request.getRatingTeaching());
        evaluation.setRatingCourseContent(request.getRatingCourseContent());
        evaluation.setRatingOverall(request.getRatingOverall());
        evaluation.setComments(request.getComments());

        CourseEvaluation saved = evaluationRepository.save(evaluation);
        return mapToStudentResponse(saved);
    }

    // ================================================================
    // STUDENT: My evaluations
    // ================================================================
    @Override
    public List<CourseEvaluationResponse> getMyEvaluations(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return evaluationRepository.findByStudentId(student.getId())
                .stream().map(this::mapToStudentResponse).collect(Collectors.toList());
    }

    @Override
    public CourseEvaluationResponse getMyEvaluationForSection(String studentEmail, Long sectionId) {
        Student student = getStudentByEmail(studentEmail);
        CourseEvaluation evaluation = evaluationRepository
                .findBySectionIdAndStudentId(sectionId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You have not evaluated section: " + sectionId));
        return mapToStudentResponse(evaluation);
    }

    // ================================================================
    // STUDENT: Pending evaluations (enrolled sections not yet evaluated)
    // ================================================================
    @Override
    public List<CourseSectionResponse> getMyPendingEvaluations(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);

        // Fetch all sections student is/was enrolled in
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        return enrollments.stream()
                .map(Enrollment::getSection)
                // Only past or current term sections
                .filter(s -> !s.getTerm().getStartDate().isAfter(LocalDate.now()))
                // Not yet evaluated
                .filter(s -> !evaluationRepository.existsBySectionIdAndStudentId(s.getId(), student.getId()))
                .map(this::mapToSectionResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN: All evaluations (anonymous)
    // ================================================================
    @Override
    public List<AdminEvaluationResponse> getAllEvaluations() {
        return evaluationRepository.findAll().stream()
                .map(this::mapToAdminResponse).collect(Collectors.toList());
    }

    @Override
    public List<AdminEvaluationResponse> getEvaluationsBySection(Long sectionId) {
        if (!sectionRepository.existsById(sectionId)) {
            throw new ResourceNotFoundException("Section not found: " + sectionId);
        }
        return evaluationRepository.findBySectionId(sectionId).stream()
                .map(this::mapToAdminResponse).collect(Collectors.toList());
    }

    @Override
    public List<AdminEvaluationResponse> getEvaluationsByFaculty(Long facultyId) {
        if (!facultyRepository.existsById(facultyId)) {
            throw new ResourceNotFoundException("Faculty not found: " + facultyId);
        }
        return evaluationRepository.findByFacultyId(facultyId).stream()
                .map(this::mapToAdminResponse).collect(Collectors.toList());
    }

    @Override
    public List<AdminEvaluationResponse> getEvaluationsByCourse(Long courseId) {
        return evaluationRepository.findByCourseId(courseId).stream()
                .map(this::mapToAdminResponse).collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN: Aggregated summaries
    // ================================================================
    @Override
    public SectionEvaluationSummary getSectionSummary(Long sectionId) {
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        Object[] avg = evaluationRepository.getSectionAverages(sectionId);
        long count = avg[3] != null ? ((Number) avg[3]).longValue() : 0L;

        return SectionEvaluationSummary.builder()
                .sectionId(section.getId())
                .sectionName(section.getSectionName())
                .courseCode(section.getCourse().getCode())
                .courseTitle(section.getCourse().getTitle())
                .termName(section.getTerm().getName())
                .facultyFullName(section.getFaculty() != null
                        ? section.getFaculty().getUser().getFirstName() + " "
                          + section.getFaculty().getUser().getLastName()
                        : null)
                .totalEvaluations(count)
                .avgTeaching(toDouble(avg[0]))
                .avgCourseContent(toDouble(avg[1]))
                .avgOverall(toDouble(avg[2]))
                .build();
    }

    @Override
    public FacultyEvaluationSummary getFacultySummary(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyId));

        Object[] avg = evaluationRepository.getFacultyAverages(facultyId);
        long count = avg[3] != null ? ((Number) avg[3]).longValue() : 0L;

        return FacultyEvaluationSummary.builder()
                .facultyId(faculty.getId())
                .facultyFullName(faculty.getUser().getFirstName() + " " + faculty.getUser().getLastName())
                .employeeId(faculty.getEmployeeId())
                .departmentName(faculty.getDepartment().getName())
                .totalEvaluations(count)
                .avgTeaching(toDouble(avg[0]))
                .avgCourseContent(toDouble(avg[1]))
                .avgOverall(toDouble(avg[2]))
                .build();
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Student getStudentByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
    }

    private double toDouble(Object o) {
        if (o == null) return 0.0;
        double d = ((Number) o).doubleValue();
        return Math.round(d * 100.0) / 100.0; // Round to 2 decimals
    }

    private CourseEvaluationResponse mapToStudentResponse(CourseEvaluation e) {
        CourseSection s = e.getSection();
        return CourseEvaluationResponse.builder()
                .id(e.getId())
                .ratingTeaching(e.getRatingTeaching())
                .ratingCourseContent(e.getRatingCourseContent())
                .ratingOverall(e.getRatingOverall())
                .comments(e.getComments())
                .submittedAt(e.getSubmittedAt())
                .sectionId(s.getId())
                .sectionName(s.getSectionName())
                .courseCode(s.getCourse().getCode())
                .courseTitle(s.getCourse().getTitle())
                .termName(s.getTerm().getName())
                .build();
    }

    private AdminEvaluationResponse mapToAdminResponse(CourseEvaluation e) {
        CourseSection s = e.getSection();
        Faculty f = s.getFaculty();

        return AdminEvaluationResponse.builder()
                .id(e.getId())
                .ratingTeaching(e.getRatingTeaching())
                .ratingCourseContent(e.getRatingCourseContent())
                .ratingOverall(e.getRatingOverall())
                .comments(e.getComments())
                .submittedAt(e.getSubmittedAt())
                .sectionId(s.getId())
                .sectionName(s.getSectionName())
                .courseCode(s.getCourse().getCode())
                .courseTitle(s.getCourse().getTitle())
                .termName(s.getTerm().getName())
                .facultyId(f != null ? f.getId() : null)
                .facultyFullName(f != null
                        ? f.getUser().getFirstName() + " " + f.getUser().getLastName()
                        : null)
                // NOTE: student identity NOT included
                .build();
    }

    private CourseSectionResponse mapToSectionResponse(CourseSection s) {
        CourseSectionResponse.CourseSectionResponseBuilder b = CourseSectionResponse.builder()
                .id(s.getId())
                .sectionName(s.getSectionName())
                .maxCapacity(s.getMaxCapacity())
                .currentEnrollment(s.getCurrentEnrollment())
                .availableSeats(s.getMaxCapacity() - s.getCurrentEnrollment())
                .courseId(s.getCourse().getId())
                .courseCode(s.getCourse().getCode())
                .courseTitle(s.getCourse().getTitle())
                .credits(s.getCourse().getCredits())
                .termId(s.getTerm().getId())
                .termName(s.getTerm().getName())
                .termCode(s.getTerm().getTermCode());

        if (s.getFaculty() != null) {
            b.facultyId(s.getFaculty().getId())
                    .facultyFullName(s.getFaculty().getUser().getFirstName() + " "
                            + s.getFaculty().getUser().getLastName())
                    .facultyEmployeeId(s.getFaculty().getEmployeeId());
        }
        return b.build();
    }
}