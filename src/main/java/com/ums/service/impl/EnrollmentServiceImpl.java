package com.ums.service.impl;

import com.ums.dto.CourseSectionResponse;
import com.ums.dto.EnrollmentResponse;
import com.ums.entity.CourseSection;
import com.ums.entity.Enrollment;
import com.ums.entity.Student;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.CourseSectionRepository;
import com.ums.repository.EnrollmentRepository;
import com.ums.repository.StudentRepository;
import com.ums.service.EnrollmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseSectionRepository sectionRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                 StudentRepository studentRepository,
                                 CourseSectionRepository sectionRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
    }

    // ------------------------------------------------------------------
    // 1. ENROLL
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(String studentEmail, Long sectionId) {

        // 1. Find the student
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for email: " + studentEmail));

        // 2. Find the section
        CourseSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course section not found with ID: " + sectionId));

        // 3. Section must belong to the current active term
        if (!Boolean.TRUE.equals(section.getTerm().getIsCurrent())) {
            throw new BadRequestException(
                    "Cannot enroll in a section of a non-current academic term: " + section.getTerm().getName());
        }

        // 4. Cannot enroll twice (check active enrollment only)
        boolean alreadyEnrolled = enrollmentRepository
                .existsByStudentIdAndSectionIdAndStatus(
                        student.getId(), sectionId, Enrollment.EnrollmentStatus.ENROLLED);
        if (alreadyEnrolled) {
            throw new BadRequestException("You are already enrolled in this section.");
        }

        // 5. Check capacity
        if (section.getCurrentEnrollment() >= section.getMaxCapacity()) {
            throw new BadRequestException(
                    "Section is full. Max capacity: " + section.getMaxCapacity());
        }

        // 6. Create Enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ENROLLED);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // 7. Increment section enrollment count
        section.setCurrentEnrollment(section.getCurrentEnrollment() + 1);
        sectionRepository.save(section);

        return mapToResponse(saved);
    }

    // ------------------------------------------------------------------
    // 2. DROP
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public EnrollmentResponse dropSection(String studentEmail, Long sectionId) {

        // 1. Find student
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for email: " + studentEmail));

        // 2. Find active enrollment for this section
        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndSectionId(student.getId(), sectionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You are not enrolled in section ID: " + sectionId));

        // 3. Already dropped?
        if (enrollment.getStatus() != Enrollment.EnrollmentStatus.ENROLLED) {
            throw new BadRequestException(
                    "Cannot drop: enrollment status is already " + enrollment.getStatus());
        }

        // 4. Mark as DROPPED (soft delete — never hard delete)
        enrollment.setStatus(Enrollment.EnrollmentStatus.DROPPED);
        Enrollment saved = enrollmentRepository.save(enrollment);

        // 5. Decrement section enrollment count
        CourseSection section = enrollment.getSection();
        if (section.getCurrentEnrollment() > 0) {
            section.setCurrentEnrollment(section.getCurrentEnrollment() - 1);
            sectionRepository.save(section);
        }

        return mapToResponse(saved);
    }

    // ------------------------------------------------------------------
    // 3. GET MY ENROLLMENTS
    // ------------------------------------------------------------------
    @Override
    public List<EnrollmentResponse> getMyEnrollments(String studentEmail) {

        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for email: " + studentEmail));

        return enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // 4. GET AVAILABLE SECTIONS (current term, has seats)
    // ------------------------------------------------------------------
    @Override
    public List<CourseSectionResponse> getAvailableSections(String studentEmail) {
        // Validate student exists
        studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for email: " + studentEmail));

        // Fetch all sections and filter to current term + available seats
        return sectionRepository.findAll()
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getTerm().getIsCurrent()))
                .filter(s -> s.getCurrentEnrollment() < s.getMaxCapacity())
                .map(this::mapToSectionResponse)
                .collect(Collectors.toList());
    }

    // ---------------- Helpers ----------------

    private EnrollmentResponse mapToResponse(Enrollment e) {
        CourseSection s = e.getSection();
        Student st = e.getStudent();

        EnrollmentResponse.EnrollmentResponseBuilder builder = EnrollmentResponse.builder()
                .id(e.getId())
                .status(e.getStatus())
                .enrolledAt(e.getEnrolledAt())
                .studentId(st.getId())
                .studentRollNumber(st.getRollNumber())
                .studentFullName(st.getUser().getFirstName() + " " + st.getUser().getLastName())
                .sectionId(s.getId())
                .sectionName(s.getSectionName())
                .maxCapacity(s.getMaxCapacity())
                .currentEnrollment(s.getCurrentEnrollment())
                .courseId(s.getCourse().getId())
                .courseCode(s.getCourse().getCode())
                .courseTitle(s.getCourse().getTitle())
                .credits(s.getCourse().getCredits())
                .termId(s.getTerm().getId())
                .termName(s.getTerm().getName())
                .termCode(s.getTerm().getTermCode());

        if (s.getFaculty() != null) {
            builder.facultyFullName(
                    s.getFaculty().getUser().getFirstName() + " " +
                            s.getFaculty().getUser().getLastName());
        }

        return builder.build();
    }

    private CourseSectionResponse mapToSectionResponse(CourseSection s) {
        CourseSectionResponse.CourseSectionResponseBuilder builder = CourseSectionResponse.builder()
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
            builder.facultyId(s.getFaculty().getId())
                    .facultyFullName(s.getFaculty().getUser().getFirstName() + " " +
                            s.getFaculty().getUser().getLastName())
                    .facultyEmployeeId(s.getFaculty().getEmployeeId());
        }

        return builder.build();
    }
}