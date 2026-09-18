package com.ums.controller;

import com.ums.dto.AttendanceResponse;
import com.ums.dto.BulkAttendanceRequest;
import com.ums.dto.CourseSectionResponse;
import com.ums.dto.StudentBriefResponse;
import com.ums.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faculty")
public class FacultyAttendanceController {

    private final AttendanceService attendanceService;

    public FacultyAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/my-sections")
    public ResponseEntity<List<CourseSectionResponse>> mySections(Principal principal) {
        return ResponseEntity.ok(attendanceService.getMySections(principal.getName()));
    }

    @GetMapping("/sections/{sectionId}/students")
    public ResponseEntity<List<StudentBriefResponse>> enrolledStudents(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(
                attendanceService.getEnrolledStudents(sectionId, principal.getName()));
    }

    @PostMapping("/attendance/{sectionId}/bulk")
    public ResponseEntity<List<AttendanceResponse>> markBulkAttendance(
            @PathVariable Long sectionId,
            @Valid @RequestBody BulkAttendanceRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                attendanceService.markBulkAttendance(sectionId, principal.getName(), request),
                HttpStatus.CREATED);
    }

    @GetMapping("/attendance/{sectionId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable Long sectionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        return ResponseEntity.ok(
                attendanceService.getSectionAttendanceByDate(sectionId, principal.getName(), date));
    }
}