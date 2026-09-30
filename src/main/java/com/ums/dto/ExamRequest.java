package com.ums.dto;

import com.ums.entity.Exam.ExamType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ExamRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Exam type is required")
    private ExamType examType;

    @NotNull(message = "Exam date is required")
    private LocalDate examDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Room ID is required")
    private Long roomId;
}