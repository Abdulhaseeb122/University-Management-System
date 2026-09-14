package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private Integer lectureHours;
    private Integer labHours;
    private Integer credits;
    private Boolean isElective;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
}