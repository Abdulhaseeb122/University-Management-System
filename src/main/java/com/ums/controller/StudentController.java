package com.ums.controller;

import com.ums.dto.StudentProfileResponse;
import com.ums.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // Principal.getName() returns the email from JWT token
        String email = principal.getName();
        return ResponseEntity.ok(studentService.getStudentProfile(email));
    }
}