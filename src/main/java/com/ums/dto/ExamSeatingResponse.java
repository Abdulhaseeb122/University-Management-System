package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ExamSeatingResponse {
    private Long id;
    private String seatNumber;

    // Exam info (for hall ticket)
    private Long examId;
    private String examType;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Course info
    private String courseCode;
    private String courseTitle;

    // Room info
    private String roomNumber;
    private String buildingName;

    // Student info (for admin view)
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Invigilator info
    private Long invigilatorId;
    private String invigilatorName;
}