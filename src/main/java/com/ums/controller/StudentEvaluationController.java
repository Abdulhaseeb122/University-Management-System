package com.ums.controller;

import com.ums.dto.CourseEvaluationRequest;
import com.ums.dto.CourseEvaluationResponse;
import com.ums.dto.CourseSectionResponse;
import com.ums.service.CourseEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/evaluations")
public class StudentEvaluationController {

    private final CourseEvaluationService evaluationService;

    public StudentEvaluationController(CourseEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/sections/{sectionId}")
    public ResponseEntity<CourseEvaluationResponse> submit(
            @PathVariable Long sectionId,
            @Valid @RequestBody CourseEvaluationRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                evaluationService.submitEvaluation(principal.getName(), sectionId, request),
                HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<CourseEvaluationResponse>> myEvaluations(Principal principal) {
        return ResponseEntity.ok(evaluationService.getMyEvaluations(principal.getName()));
    }

    @GetMapping("/sections/{sectionId}")
    public ResponseEntity<CourseEvaluationResponse> myEvaluationForSection(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(
                evaluationService.getMyEvaluationForSection(principal.getName(), sectionId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<CourseSectionResponse>> myPending(Principal principal) {
        return ResponseEntity.ok(
                evaluationService.getMyPendingEvaluations(principal.getName()));
    }
}