package com.ums.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Course code is required")
    @Size(max = 15, message = "Course code cannot exceed 15 characters")
    private String code;

    @NotBlank(message = "Course title is required")
    @Size(max = 100, message = "Course title cannot exceed 100 characters")
    private String title;

    private String description;

    @Min(value = 0, message = "Lecture hours cannot be negative")
    @Max(value = 20, message = "Lecture hours cannot exceed 20")
    private Integer lectureHours;

    @Min(value = 0, message = "Lab hours cannot be negative")
    @Max(value = 20, message = "Lab hours cannot exceed 20")
    private Integer labHours;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Credits must be at least 1")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;

    private Boolean isElective;
}