package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.AttendanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseSectionRepository sectionRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 CourseSectionRepository sectionRepository,
                                 FacultyRepository facultyRepository,
                                 StudentRepository studentRepository,
                                 EnrollmentRepository enrollmentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sectionRepository = sectionRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ================================================================
    // FACULTY: Get my sections
    // ================================================================
    @Override
    public List<CourseSectionResponse> getMySections(String facultyEmail) {
        Faculty faculty = facultyRepository.findByUserEmail(facultyEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found for: " + facultyEmail));

        return sectionRepository.findByFacultyId(faculty.getId())
                .stream()
                .map(this::mapSectionToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Get enrolled students in my section
    // ================================================================
    @Override
    public List<StudentBriefResponse> getEnrolledStudents(Long sectionId, String facultyEmail) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        // Only currently ENROLLED students
        return enrollmentRepository.findBySectionId(section.getId())
                .stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .map(Enrollment::getStudent)
                .map(s -> StudentBriefResponse.builder()
                        .id(s.getId())
                        .rollNumber(s.getRollNumber())
                        .fullName(s.getUser().getFirstName() + " " + s.getUser().getLastName())
                        .email(s.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: Mark attendance (bulk)
    // ================================================================
    @Override
    @Transactional
    public List<AttendanceResponse> markBulkAttendance(Long sectionId,
                                                       String facultyEmail,
                                                       BulkAttendanceRequest request) {

        CourseSection section = getOwnedSection(sectionId, facultyEmail);
        Faculty faculty = section.getFaculty();

        // 1. Cannot mark future dates
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Cannot mark attendance for a future date.");
        }

        List<Attendance> savedRecords = new ArrayList<>();

        for (AttendanceRecordRequest record : request.getRecords()) {
            Student student = studentRepository.findById(record.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Student not found with ID: " + record.getStudentId()));

            // 2. Student must be actively enrolled in this section
            boolean isEnrolled = enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                    student.getId(), sectionId, Enrollment.EnrollmentStatus.ENROLLED);
            if (!isEnrolled) {
                throw new BadRequestException(
                        "Student " + student.getRollNumber() + " is not enrolled in this section.");
            }

            // 3. Already marked for this date?
            if (attendanceRepository.existsBySectionIdAndStudentIdAndDate(
                    sectionId, student.getId(), request.getDate())) {
                throw new BadRequestException(
                        "Attendance already recorded for student " + student.getRollNumber()
                                + " on " + request.getDate());
            }

            Attendance attendance = new Attendance();
            attendance.setSection(section);
            attendance.setStudent(student);
            attendance.setDate(request.getDate());
            attendance.setStatus(record.getStatus());
            attendance.setRemarks(record.getRemarks());
            attendance.setRecordedBy(faculty);

            savedRecords.add(attendanceRepository.save(attendance));
        }

        return savedRecords.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // FACULTY: View attendance for a date
    // ================================================================
    @Override
    public List<AttendanceResponse> getSectionAttendanceByDate(Long sectionId,
                                                               String facultyEmail,
                                                               LocalDate date) {
        CourseSection section = getOwnedSection(sectionId, facultyEmail);

        return attendanceRepository.findBySectionIdAndDate(section.getId(), date)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: Get my attendance for a section
    // ================================================================
    @Override
    public List<AttendanceResponse> getMyAttendanceInSection(Long sectionId, String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        // Verify student is enrolled in this section
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndSectionIdAndStatus(
                student.getId(), sectionId, Enrollment.EnrollmentStatus.ENROLLED);
        if (!isEnrolled) {
            throw new BadRequestException("You are not enrolled in this section.");
        }

        return attendanceRepository.findByStudentIdAndSectionId(student.getId(), sectionId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: Summary across all sections
    // ================================================================
    @Override
    public List<AttendanceSummaryResponse> getMyAttendanceSummary(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        // Get all enrollments (active + dropped) — but only show enrolled sections
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .collect(Collectors.toList());

        List<AttendanceSummaryResponse> summaries = new ArrayList<>();

        for (Enrollment e : enrollments) {
            CourseSection section = e.getSection();
            List<Attendance> records = attendanceRepository.findByStudentIdAndSectionId(
                    student.getId(), section.getId());

            long present = records.stream()
                    .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT).count();
            long absent = records.stream()
                    .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT).count();
            long late = records.stream()
                    .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LATE).count();
            long excused = records.stream()
                    .filter(a -> a.getStatus() == Attendance.AttendanceStatus.EXCUSED).count();

            long total = records.size();
            double percentage = total == 0 ? 100.0 : ((present + excused) * 100.0) / total;

            summaries.add(AttendanceSummaryResponse.builder()
                    .sectionId(section.getId())
                    .sectionName(section.getSectionName())
                    .courseCode(section.getCourse().getCode())
                    .courseTitle(section.getCourse().getTitle())
                    .totalClasses(total)
                    .present(present)
                    .absent(absent)
                    .late(late)
                    .excused(excused)
                    .attendancePercentage(Math.round(percentage * 100.0) / 100.0)
                    .belowThreshold(percentage < 75.0)
                    .build());
        }

        return summaries;
    }

    // ================================================================
    // HELPERS
    // ================================================================

    /**
     * Fetch section and verify it belongs to the requesting faculty.
     * Blocks unauthorized access (IDOR attack protection).
     */
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

    private AttendanceResponse mapToResponse(Attendance a) {
        Student s = a.getStudent();
        CourseSection sec = a.getSection();

        return AttendanceResponse.builder()
                .id(a.getId())
                .date(a.getDate())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .studentId(s.getId())
                .studentRollNumber(s.getRollNumber())
                .studentFullName(s.getUser().getFirstName() + " " + s.getUser().getLastName())
                .sectionId(sec.getId())
                .sectionName(sec.getSectionName())
                .courseCode(sec.getCourse().getCode())
                .recordedByFullName(a.getRecordedBy() != null
                        ? a.getRecordedBy().getUser().getFirstName() + " "
                          + a.getRecordedBy().getUser().getLastName()
                        : null)
                .build();
    }

    private CourseSectionResponse mapSectionToResponse(CourseSection s) {
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
                    .facultyFullName(s.getFaculty().getUser().getFirstName() + " "
                            + s.getFaculty().getUser().getLastName())
                    .facultyEmployeeId(s.getFaculty().getEmployeeId());
        }

        return builder.build();
    }
}