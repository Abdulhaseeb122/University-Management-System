package com.ums.controller;

import com.ums.dto.ExamSeatingResponse;
import com.ums.service.ExamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty/exams")
public class FacultyExamController {

    private final ExamService examService;

    public FacultyExamController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/invigilation")
    public ResponseEntity<List<ExamSeatingResponse>> myInvigilations(Principal principal) {
        return ResponseEntity.ok(examService.getMyInvigilations(principal.getName()));
    }

    @GetMapping("/{examId}/students")
    public ResponseEntity<List<ExamSeatingResponse>> studentsForExam(
            @PathVariable Long examId, Principal principal) {
        return ResponseEntity.ok(
                examService.getStudentsForInvigilatedExam(principal.getName(), examId));
    }
}