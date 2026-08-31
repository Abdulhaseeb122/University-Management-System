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
    private String code; // e.g., CS101[cite: 1]

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    private String description;

    @Min(value = 0, message = "Lecture hours cannot be negative")
    private Integer lectureHours = 3; //[cite: 1]

    @Min(value = 0, message = "Lab hours cannot be negative")
    private Integer labHours = 0; //[cite: 1]

    @NotNull(message = "Credits value is required")
    @Min(value = 1, message = "Course must offer at least 1 credit")
    private Integer credits; //[cite: 1]

    private Boolean isElective = false; //[cite: 1]
}