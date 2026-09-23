package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FinalCourseGradeResponse {
    private Long id;
    private BigDecimal totalScore;
    private String letterGrade;
    private BigDecimal gradePoint;
    private Boolean isPublished;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Course & section info
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Integer credits;
    private String sectionName;
    private String termName;
}