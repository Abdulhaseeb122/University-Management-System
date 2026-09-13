package com.ums.controller;

import com.ums.dto.StudentProfileResponse;
import com.ums.dto.StudentUpdateRequest;
import com.ums.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(studentService.getStudentProfile(principal.getName()));
    }

    // ---------------- NEW ENDPOINT ----------------
    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateMyProfile(
            Principal principal,
            @Valid @RequestBody StudentUpdateRequest request) {
        return ResponseEntity.ok(studentService.updateStudentProfile(principal.getName(), request));
    }
}