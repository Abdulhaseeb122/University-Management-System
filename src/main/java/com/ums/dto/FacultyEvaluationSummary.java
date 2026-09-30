package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyEvaluationSummary {
    private Long facultyId;
    private String facultyFullName;
    private String employeeId;
    private String departmentName;

    private long totalEvaluations;
    private double avgTeaching;
    private double avgCourseContent;
    private double avgOverall;
}