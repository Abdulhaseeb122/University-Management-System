package com.ums.controller;

import com.ums.dto.FinalCourseGradeResponse;
import com.ums.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
public class StudentGradeController {

    private final GradeService gradeService;

    public StudentGradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping("/grades/section/{sectionId}")
    public ResponseEntity<List<FinalCourseGradeResponse>> mySectionGrades(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(gradeService.getMyGradesForSection(sectionId, principal.getName()));
    }

    @GetMapping("/transcript")
    public ResponseEntity<List<FinalCourseGradeResponse>> myTranscript(Principal principal) {
        return ResponseEntity.ok(gradeService.getMyTranscript(principal.getName()));
    }
}