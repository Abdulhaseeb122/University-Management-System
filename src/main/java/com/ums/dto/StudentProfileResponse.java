package com.ums.dto;

import com.ums.entity.Student.AcademicStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class StudentProfileResponse {
    private Long id;
    private String rollNumber;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String departmentName;
    private Integer currentSemester;
    private LocalDate admissionDate;
    private AcademicStatus academicStatus;
    private BigDecimal cgpa;
    private Integer totalCreditsEarned;
}