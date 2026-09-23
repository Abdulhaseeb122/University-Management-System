package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.GradeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeServiceImpl implements GradeService {

    private final GradeItemRepository gradeItemRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final FinalCourseGradeRepository finalGradeRepository;
    private final CourseSectionRepository sectionRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public GradeServiceImpl(GradeItemRepository gradeItemRepository,
                            StudentGradeRepository studentGradeRepository,
                            FinalCourseGradeRepository finalGradeRepository,
                            CourseSectionRepository sectionRepository,
                            FacultyRepository facultyRepository,
                            StudentRepository studentRepository,
                            EnrollmentRepository enrollmentRepository) {
        this.gradeItemRepository = gradeItemRepository;
        this.studentGradeRepository = studentGradeRepository;
        this.finalGradeRepository = finalGradeRepository;
        this.sectionRepository = sectionRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ================================================================
    // FACULTY: Create Grade Item
    // ================================================================
    @Override
    @Transactional
    public GradeItemResponse createGradeItem(Long sectionId, String facultyEmail, GradeItemRequest request) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        // Validate: total weightage cannot exceed 100
        BigDecimal existingWeight = gradeItemRepository.sumWeightageBySectionId(sectionId);
        BigDecimal newTotal = existingWeight.add(request.getWeightagePercent());

        if (newTotal.compareTo(new BigDecimal("100.00")) > 0) {
            throw new BadRequestException(
                    "Total weightage would exceed 100%. Current: " + existingWeight
                            + "%, Adding: " + request.getWeightagePercent() + "%");
        }

        GradeItem item = new GradeItem();
        item.setSection(section);
        item.setTitle(request.getTitle());
        item.setItemType(request.getItemType());
        item.setMaxMarks(request.getMaxMarks());
        item.setWeightagePercent(request.getWeightagePercent());
        item.setDueDate(request.getDueDate());

        GradeItem saved = gradeItemRepository.save(item);
        return mapItemToResponse(saved);
    }

    // ================================================================
    // FACULTY: List Grade Items
    // ================================================================
    @Override
    public List<GradeItemResponse> getGradeItems(Long sectionId, String facultyEmail) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        return gradeItemRepository.findBySectionId(section.getId())
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Delete Grade Item
    // ================================================================
    @Override
    @Transactional
    public void deleteGradeItem(Long gradeItemId, String facultyEmail) {
        GradeItem item = gradeItemRepository.findById(gradeItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade item not found: " + gradeItemId));

        // Ensure faculty owns the section
        getOwnedSection(item.getSection().getId(), facultyEmail);

        // Safety: cannot delete if marks already entered
        if (studentGradeRepository.existsByGradeItemId(gradeItemId)) {
            throw new BadRequestException("Cannot delete: marks already entered for this item.");
        }

        gradeItemRepository.delete(item);
    }

    // ================================================================
    // FACULTY: Enter Bulk Marks (upsert)
    // ================================================================
    @Override
    @Transactional
    public List<StudentGradeResponse> enterBulkMarks(Long gradeItemId, String facultyEmail, BulkMarksRequest request) {
        GradeItem item = gradeItemRepository.findById(gradeItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade item not found: " + gradeItemId));

        getOwnedSection(item.getSection().getId(), facultyEmail);

        List<StudentGrade> savedRecords = new ArrayList<>();

        for (MarksRecord record : request.getMarks()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found: " + record.getStudentId()));

            // Validate marks do not exceed max
            if (record.getMarksObtained().compareTo(item.getMaxMarks()) > 0) {
                throw new BadRequestException(
                        "Marks (" + record.getMarksObtained() + ") exceed max marks (" + item.getMaxMarks()
                                + ") for " + item.getTitle());
            }

            // Student must be enrolled in this section
            boolean enrolled = enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                    student.getId(), item.getSection().getId(), Enrollment.EnrollmentStatus.ENROLLED);
            if (!enrolled) {
                throw new BadRequestException(
                        "Student " + student.getRollNumber() + " is not enrolled in this section.");
            }

            // Upsert
            StudentGrade grade = studentGradeRepository
                    .findByGradeItemIdAndStudentId(gradeItemId, student.getId())
                    .orElseGet(StudentGrade::new);

            grade.setGradeItem(item);
            grade.setStudent(student);
            grade.setMarksObtained(record.getMarksObtained());
            grade.setFeedback(record.getFeedback());

            savedRecords.add(studentGradeRepository.save(grade));
        }

        return savedRecords.stream().map(this::mapGradeToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Get marks for a grade item
    // ================================================================
    @Override
    public List<StudentGradeResponse> getMarksForGradeItem(Long gradeItemId, String facultyEmail) {
        GradeItem item = gradeItemRepository.findById(gradeItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade item not found: " + gradeItemId));

        getOwnedSection(item.getSection().getId(), facultyEmail);

        return studentGradeRepository.findByGradeItemId(gradeItemId)
                .stream()
                .map(this::mapGradeToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Compute Final Grades for all enrolled students
    // ================================================================
    @Override
    @Transactional
    public List<FinalCourseGradeResponse> computeFinalGrades(Long sectionId, String facultyEmail) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        List<Enrollment> enrollments = enrollmentRepository.findBySectionId(sectionId)
                .stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .collect(Collectors.toList());

        List<FinalCourseGrade> results = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Student student = e.getStudent();

            // Fetch all marks in this section for this student
            List<StudentGrade> grades = studentGradeRepository
                    .findByStudentIdAndGradeItemSectionId(student.getId(), sectionId);

            // Weighted total = sum( marks/max * weight )
            BigDecimal total = BigDecimal.ZERO;
            for (StudentGrade g : grades) {
                BigDecimal ratio = g.getMarksObtained()
                        .divide(g.getGradeItem().getMaxMarks(), 4, RoundingMode.HALF_UP);
                BigDecimal contribution = ratio.multiply(g.getGradeItem().getWeightagePercent());
                total = total.add(contribution);
            }
            total = total.setScale(2, RoundingMode.HALF_UP);

            // Compute letter grade and GPA point
            String[] lg = computeLetterGrade(total);

            FinalCourseGrade fcg = finalGradeRepository.findByEnrollmentId(e.getId())
                    .orElseGet(FinalCourseGrade::new);
            fcg.setEnrollment(e);
            fcg.setTotalScore(total);
            fcg.setLetterGrade(lg[0]);
            fcg.setGradePoint(new BigDecimal(lg[1]));
            // Do NOT auto-publish; faculty must call publish endpoint
            if (fcg.getIsPublished() == null) {
                fcg.setIsPublished(false);
            }

            results.add(finalGradeRepository.save(fcg));
        }

        return results.stream().map(this::mapFinalToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Publish Final Grades + Recalculate CGPA
    // ================================================================
    @Override
    @Transactional
    public List<FinalCourseGradeResponse> publishFinalGrades(Long sectionId, String facultyEmail) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        List<FinalCourseGrade> grades = finalGradeRepository.findByEnrollmentSectionId(sectionId);

        if (grades.isEmpty()) {
            throw new BadRequestException(
                    "No final grades computed yet. Please run 'finalize' first.");
        }

        // Collect affected student IDs
        List<Long> studentIds = new ArrayList<>();
        for (FinalCourseGrade fcg : grades) {
            fcg.setIsPublished(true);
            finalGradeRepository.save(fcg);
            studentIds.add(fcg.getEnrollment().getStudent().getId());
        }

        // Recalculate CGPA for every affected student
        for (Long sid : studentIds) {
            recalculateCgpa(sid);
        }

        return grades.stream().map(this::mapFinalToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: View grades for a section (published only)
    // ================================================================
    @Override
    public List<FinalCourseGradeResponse> getMyGradesForSection(Long sectionId, String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        return finalGradeRepository.findByEnrollmentSectionId(sectionId)
                .stream()
                .filter(f -> f.getEnrollment().getStudent().getId().equals(student.getId()))
                .filter(f -> Boolean.TRUE.equals(f.getIsPublished()))
                .map(this::mapFinalToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: Full transcript (all published grades)
    // ================================================================
    @Override
    public List<FinalCourseGradeResponse> getMyTranscript(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        return finalGradeRepository.findByEnrollmentStudentIdAndIsPublishedTrue(student.getId())
                .stream()
                .map(this::mapFinalToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private CourseSection getOwnedSection(Long sectionId, String facultyEmail) {
        Faculty faculty = facultyRepository.findByUserEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found: " + facultyEmail));

        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found: " + sectionId));

        if (section.getFaculty() == null
                || !section.getFaculty().getId().equals(faculty.getId())) {
            throw new BadRequestException("You are not assigned to this section.");
        }

        return section;
    }

    /**
     * Convert numeric score (0-100) to [letterGrade, gradePoint].
     * Industry-standard 4.0 scale.
     */
    private String[] computeLetterGrade(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 90) return new String[]{"A",  "4.00"};
        if (s >= 85) return new String[]{"A-", "3.70"};
        if (s >= 80) return new String[]{"B+", "3.30"};
        if (s >= 75) return new String[]{"B",  "3.00"};
        if (s >= 70) return new String[]{"B-", "2.70"};
        if (s >= 65) return new String[]{"C+", "2.30"};
        if (s >= 60) return new String[]{"C",  "2.00"};
        if (s >= 55) return new String[]{"C-", "1.70"};
        if (s >= 50) return new String[]{"D",  "1.00"};
        return new String[]{"F", "0.00"};
    }

    /**
     * Recalculate CGPA across all PUBLISHED final grades for the student.
     * CGPA = sum(gradePoint * credits) / sum(credits)
     */
    private void recalculateCgpa(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        List<FinalCourseGrade> published = finalGradeRepository
                .findByEnrollmentStudentIdAndIsPublishedTrue(studentId);

        BigDecimal totalPoints = BigDecimal.ZERO;
        int totalCredits = 0;

        for (FinalCourseGrade fcg : published) {
            Course course = fcg.getEnrollment().getSection().getCourse();
            int credits = course.getCredits();
            BigDecimal gp = fcg.getGradePoint() != null ? fcg.getGradePoint() : BigDecimal.ZERO;

            totalPoints = totalPoints.add(gp.multiply(BigDecimal.valueOf(credits)));
            totalCredits += credits;
        }

        if (totalCredits == 0) {
            student.setCgpa(BigDecimal.ZERO);
            student.setTotalCreditsEarned(0);
        } else {
            BigDecimal cgpa = totalPoints.divide(
                    BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
            student.setCgpa(cgpa);
            student.setTotalCreditsEarned(totalCredits);
        }

        studentRepository.save(student);
    }

    // ---------------- Mappers ----------------

    private GradeItemResponse mapItemToResponse(GradeItem g) {
        return GradeItemResponse.builder()
                .id(g.getId())
                .title(g.getTitle())
                .itemType(g.getItemType())
                .maxMarks(g.getMaxMarks())
                .weightagePercent(g.getWeightagePercent())
                .dueDate(g.getDueDate())
                .sectionId(g.getSection().getId())
                .sectionName(g.getSection().getSectionName())
                .courseCode(g.getSection().getCourse().getCode())
                .build();
    }

    private StudentGradeResponse mapGradeToResponse(StudentGrade g) {
        return StudentGradeResponse.builder()
                .id(g.getId())
                .marksObtained(g.getMarksObtained())
                .feedback(g.getFeedback())
                .gradedAt(g.getGradedAt())
                .studentId(g.getStudent().getId())
                .studentRollNumber(g.getStudent().getRollNumber())
                .studentFullName(g.getStudent().getUser().getFirstName() + " "
                        + g.getStudent().getUser().getLastName())
                .gradeItemId(g.getGradeItem().getId())
                .gradeItemTitle(g.getGradeItem().getTitle())
                .maxMarks(g.getGradeItem().getMaxMarks())
                .weightagePercent(g.getGradeItem().getWeightagePercent())
                .build();
    }

    private FinalCourseGradeResponse mapFinalToResponse(FinalCourseGrade f) {
        CourseSection section = f.getEnrollment().getSection();
        Course course = section.getCourse();
        Student student = f.getEnrollment().getStudent();

        return FinalCourseGradeResponse.builder()
                .id(f.getId())
                .totalScore(f.getTotalScore())
                .letterGrade(f.getLetterGrade())
                .gradePoint(f.getGradePoint())
                .isPublished(f.getIsPublished())
                .studentId(student.getId())
                .studentRollNumber(student.getRollNumber())
                .studentFullName(student.getUser().getFirstName() + " "
                        + student.getUser().getLastName())
                .courseId(course.getId())
                .courseCode(course.getCode())
                .courseTitle(course.getTitle())
                .credits(course.getCredits())
                .sectionName(section.getSectionName())
                .termName(section.getTerm().getName())
                .build();
    }
}