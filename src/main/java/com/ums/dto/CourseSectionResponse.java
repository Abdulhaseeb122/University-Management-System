package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseSectionResponse {
    private Long id;
    private String sectionName;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private Integer availableSeats;

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
    private Long facultyId;
    private String facultyFullName;
    private String facultyEmployeeId;
}