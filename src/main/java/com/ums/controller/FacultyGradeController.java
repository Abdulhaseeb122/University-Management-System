package com.ums.controller;

import com.ums.dto.*;
import com.ums.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
public class FacultyGradeController {

    private final GradeService gradeService;

    public FacultyGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    // ---- Grade Items ----

    @PostMapping("/sections/{sectionId}/grade-items")
    public ResponseEntity<GradeItemResponse> createGradeItem(
            @PathVariable Long sectionId,
            @Valid @RequestBody GradeItemRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                gradeService.createGradeItem(sectionId, principal.getName(), request),
                HttpStatus.CREATED);
    }

    @GetMapping("/sections/{sectionId}/grade-items")
    public ResponseEntity<List<GradeItemResponse>> getGradeItems(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(gradeService.getGradeItems(sectionId, principal.getName()));
    }

    @DeleteMapping("/grade-items/{gradeItemId}")
    public ResponseEntity<Void> deleteGradeItem(@PathVariable Long gradeItemId, Principal principal) {
        gradeService.deleteGradeItem(gradeItemId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // ---- Marks ----

    @PostMapping("/grade-items/{gradeItemId}/marks")
    public ResponseEntity<List<StudentGradeResponse>> enterMarks(
            @PathVariable Long gradeItemId,
            @Valid @RequestBody BulkMarksRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                gradeService.enterBulkMarks(gradeItemId, principal.getName(), request),
                HttpStatus.CREATED);
    }

    @GetMapping("/grade-items/{gradeItemId}/marks")
    public ResponseEntity<List<StudentGradeResponse>> getMarks(
            @PathVariable Long gradeItemId, Principal principal) {
        return ResponseEntity.ok(gradeService.getMarksForGradeItem(gradeItemId, principal.getName()));
    }

    // ---- Final Grades ----

    @PostMapping("/sections/{sectionId}/finalize")
    public ResponseEntity<List<FinalCourseGradeResponse>> finalize(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(gradeService.computeFinalGrades(sectionId, principal.getName()));
    }

    @PostMapping("/sections/{sectionId}/publish")
    public ResponseEntity<List<FinalCourseGradeResponse>> publish(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(gradeService.publishFinalGrades(sectionId, principal.getName()));
    }
}