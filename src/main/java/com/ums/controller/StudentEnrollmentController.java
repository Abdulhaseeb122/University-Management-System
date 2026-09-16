package com.ums.controller;

import com.ums.dto.CourseSectionResponse;
import com.ums.dto.EnrollmentResponse;
import com.ums.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;

    public StudentEnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Enroll in a section
    @PostMapping("/enroll/{sectionId}")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long sectionId,
                                                     Principal principal) {
        EnrollmentResponse response = enrollmentService.enrollStudent(principal.getName(), sectionId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Drop a section
    @DeleteMapping("/drop/{sectionId}")
    public ResponseEntity<EnrollmentResponse> drop(@PathVariable Long sectionId,
                                                   Principal principal) {
        return ResponseEntity.ok(enrollmentService.dropSection(principal.getName(), sectionId));
    }

    // View my enrollments
    @GetMapping("/my-enrollments")
    public ResponseEntity<List<EnrollmentResponse>> myEnrollments(Principal principal) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(principal.getName()));
    }

    // Browse sections available for enrollment
    @GetMapping("/available-sections")
    public ResponseEntity<List<CourseSectionResponse>> availableSections(Principal principal) {
        return ResponseEntity.ok(enrollmentService.getAvailableSections(principal.getName()));
    }
}