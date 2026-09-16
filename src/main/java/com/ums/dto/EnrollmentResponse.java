package com.ums.dto;

import com.ums.entity.Enrollment.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EnrollmentResponse {
    private Long id;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;

    // Student info (compact)
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Section info
    private Long sectionId;
    private String sectionName;
    private Integer maxCapacity;
    private Integer currentEnrollment;

    // Course info
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Integer credits;

    // Term info
    private Long termId;
    private String termName;
    private String termCode;

    // Faculty info (nullable)
    private String facultyFullName;
}