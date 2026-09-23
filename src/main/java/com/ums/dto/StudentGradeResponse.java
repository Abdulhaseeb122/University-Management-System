package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StudentGradeResponse {
    private Long id;
    private BigDecimal marksObtained;
    private String feedback;
    private LocalDateTime gradedAt;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Grade item info
    private Long gradeItemId;
    private String gradeItemTitle;
    private BigDecimal maxMarks;
    private BigDecimal weightagePercent;
}