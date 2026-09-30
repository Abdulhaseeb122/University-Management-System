package com.ums.controller;

import com.ums.dto.ExamResponse;
import com.ums.dto.ExamSeatingResponse;
import com.ums.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/exams")
public class StudentExamController {

    private final ExamService examService;

    public StudentExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ExamResponse>> upcoming(Principal principal) {
        return ResponseEntity.ok(examService.getMyUpcomingExams(principal.getName()));
    }

    @GetMapping("/my-seating")
    public ResponseEntity<List<ExamSeatingResponse>> mySeating(Principal principal) {
        return ResponseEntity.ok(examService.getMySeating(principal.getName()));
    }

    @GetMapping("/{examId}/seat")
    public ResponseEntity<ExamSeatingResponse> mySeat(
            @PathVariable Long examId, Principal principal) {
        return ResponseEntity.ok(
                examService.getMySeatForExam(principal.getName(), examId));
    }
}