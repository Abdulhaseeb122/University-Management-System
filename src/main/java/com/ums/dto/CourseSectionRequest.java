package com.ums.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseSectionRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotBlank(message = "Section name is required")
    @Size(max = 10, message = "Section name cannot exceed 10 characters")
    private String sectionName;

    // Faculty is optional — admin can assign later
    private Long facultyId;

    @NotNull(message = "Max capacity is required")
    @Min(value = 1, message = "Max capacity must be at least 1")
    @Max(value = 500, message = "Max capacity cannot exceed 500")
    private Integer maxCapacity;
}