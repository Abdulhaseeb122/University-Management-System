package com.ums.controller;

import com.ums.dto.FreezeApplicationRequest;
import com.ums.dto.FreezeResponse;
import com.ums.dto.ResumeRequest;
import com.ums.service.SemesterFreezeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/freeze")
public class StudentFreezeController {

    private final SemesterFreezeService freezeService;

    public StudentFreezeController(SemesterFreezeService freezeService) {
        this.freezeService = freezeService;
    }

    @PostMapping("/apply")
    public ResponseEntity<FreezeResponse> apply(
            @Valid @RequestBody FreezeApplicationRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                freezeService.applyForFreeze(principal.getName(), request),
                HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<List<FreezeResponse>> myHistory(Principal principal) {
        return ResponseEntity.ok(freezeService.getMyFreezeHistory(principal.getName()));
    }

    @GetMapping("/active")
    public ResponseEntity<FreezeResponse> active(Principal principal) {
        return ResponseEntity.ok(freezeService.getMyActiveFreeze(principal.getName()));
    }

    @PostMapping("/resume")
    public ResponseEntity<FreezeResponse> resume(
            @Valid @RequestBody ResumeRequest request,
            Principal principal) {
        return ResponseEntity.ok(freezeService.requestResume(principal.getName(), request));
    }
}