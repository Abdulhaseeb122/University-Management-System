package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminEvaluationResponse {
    private Long id;
    private Integer ratingTeaching;
    private Integer ratingCourseContent;
    private Integer ratingOverall;
    private String comments;
    private LocalDateTime submittedAt;

    // Section info
    private Long sectionId;
    private String sectionName;
    private String courseCode;
    private String courseTitle;
    private String termName;

    // Faculty info
    private Long facultyId;
    private String facultyFullName;

    // NOTE: Student identity intentionally omitted — anonymity
}