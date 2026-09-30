package com.ums.dto;

import com.ums.entity.Exam.ExamType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ExamResponse {
    private Long id;
    private ExamType examType;
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Term info
    private Long termId;
    private String termName;
    private String termCode;

    // Course info
    private Long courseId;
    private String courseCode;
    private String courseTitle;

    // Room info
    private Long roomId;
    private String roomNumber;
    private String buildingName;
    private Integer roomCapacity;

    // Derived
    private long allocatedSeats;
    private long availableSeats;
}