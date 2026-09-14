package com.ums.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AcademicTermRequest {

    @NotBlank(message = "Term name is required")
    @Size(max = 50, message = "Term name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Term code is required")
    @Size(max = 15, message = "Term code cannot exceed 15 characters")
    private String termCode;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    // Optional: If true, this term becomes the currently active one
    private Boolean isCurrent;
}