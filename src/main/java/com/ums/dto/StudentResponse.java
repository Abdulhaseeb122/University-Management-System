package com.ums.dto;

import com.ums.entity.Student.AcademicStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {

    private Long id;
    private String rollNumber; //
    private String fullName;
    private String email;
    private String phoneNumber;
    private String departmentName;
    private String campusName;
    private Integer currentSemester; //[cite: 1]
    private LocalDate admissionDate; //[cite: 1]
    private AcademicStatus academicStatus; //[cite: 1]
    private BigDecimal cgpa; //[cite: 1]
    private Integer totalCreditsEarned; //[cite: 1]
}