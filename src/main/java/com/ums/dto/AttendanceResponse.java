package com.ums.dto;

import com.ums.entity.Attendance.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AttendanceResponse {
    private Long id;
    private LocalDate date;
    private AttendanceStatus status;
    private String remarks;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Section info
    private Long sectionId;
    private String sectionName;
    private String courseCode;

    // Who recorded
    private String recordedByFullName;
}