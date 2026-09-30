package com.ums.controller;

import com.ums.dto.*;
import com.ums.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/exams")
public class AdminExamController {

    private final ExamService examService;

    public AdminExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
        return new ResponseEntity<>(examService.createExam(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> getAll() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(examService.updateExam(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/term/{termId}")
    public ResponseEntity<List<ExamResponse>> byTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(examService.getExamsByTerm(termId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<ExamResponse>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(examService.getExamsByCourse(courseId));
    }

    // ---------- Seating ----------

    @PostMapping("/{id}/seat-allocation")
    public ResponseEntity<List<ExamSeatingResponse>> allocate(@PathVariable Long id) {
        return new ResponseEntity<>(examService.allocateSeats(id), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/seating")
    public ResponseEntity<List<ExamSeatingResponse>> getSeating(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamSeating(id));
    }

    @DeleteMapping("/{id}/seating")
    public ResponseEntity<Void> clearSeating(@PathVariable Long id) {
        examService.clearSeating(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/invigilators")
    public ResponseEntity<List<ExamSeatingResponse>> assignInvigilator(
            @PathVariable Long id,
            @Valid @RequestBody AssignInvigilatorRequest request) {
        return ResponseEntity.ok(
                examService.assignInvigilator(id, request.getFacultyId()));
    }
}