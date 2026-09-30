package com.ums.controller;

import com.ums.dto.AdminEvaluationResponse;
import com.ums.dto.FacultyEvaluationSummary;
import com.ums.dto.SectionEvaluationSummary;
import com.ums.service.CourseEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/evaluations")
public class AdminEvaluationController {

    private final CourseEvaluationService evaluationService;

    public AdminEvaluationController(CourseEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminEvaluationResponse>> getAll() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<List<AdminEvaluationResponse>> bySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsBySection(sectionId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<AdminEvaluationResponse>> byFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByFaculty(facultyId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<AdminEvaluationResponse>> byCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByCourse(courseId));
    }

    @GetMapping("/section/{sectionId}/summary")
    public ResponseEntity<SectionEvaluationSummary> sectionSummary(@PathVariable Long sectionId) {
        return ResponseEntity.ok(evaluationService.getSectionSummary(sectionId));
    }

    @GetMapping("/faculty/{facultyId}/summary")
    public ResponseEntity<FacultyEvaluationSummary> facultySummary(@PathVariable Long facultyId) {
        return ResponseEntity.ok(evaluationService.getFacultySummary(facultyId));
    }
}