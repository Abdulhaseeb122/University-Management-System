package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CourseEvaluationResponse {
    private Long id;
    private Integer ratingTeaching;
    private Integer ratingCourseContent;
    private Integer ratingOverall;
    private String comments;
    private LocalDateTime submittedAt;

    // Section info (safe to show)
    private Long sectionId;
    private String sectionName;
    private String courseCode;
    private String courseTitle;
    private String termName;
    // NOTE: Student info NOT included (anonymity)
}