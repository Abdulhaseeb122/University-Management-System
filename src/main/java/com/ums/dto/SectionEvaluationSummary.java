package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionEvaluationSummary {
    private Long sectionId;
    private String sectionName;
    private String courseCode;
    private String courseTitle;
    private String termName;
    private String facultyFullName;

    private long totalEvaluations;
    private double avgTeaching;
    private double avgCourseContent;
    private double avgOverall;
}