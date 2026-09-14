package com.ums.dto;

import com.ums.entity.User.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FacultyResponse {
    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String departmentName;
    private String departmentCode;
    private String designation;
    private LocalDate joiningDate;
    private String officeRoomNumber;
    private String specialization;
}