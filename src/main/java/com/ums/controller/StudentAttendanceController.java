package com.ums.controller;

import com.ums.dto.AttendanceResponse;
import com.ums.dto.AttendanceSummaryResponse;
import com.ums.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student")
public class StudentAttendanceController {

    private final AttendanceService attendanceService;

    public StudentAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/attendance-summary")
    public ResponseEntity<List<AttendanceSummaryResponse>> mySummary(Principal principal) {
        return ResponseEntity.ok(attendanceService.getMyAttendanceSummary(principal.getName()));
    }

    @GetMapping("/attendance/{sectionId}")
    public ResponseEntity<List<AttendanceResponse>> myAttendanceInSection(
            @PathVariable Long sectionId, Principal principal) {
        return ResponseEntity.ok(
                attendanceService.getMyAttendanceInSection(sectionId, principal.getName()));
    }
}