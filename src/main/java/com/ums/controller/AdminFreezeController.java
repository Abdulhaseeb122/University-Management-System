package com.ums.controller;

import com.ums.dto.FreezeApprovalRequest;
import com.ums.dto.FreezeResponse;
import com.ums.service.SemesterFreezeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/freeze")
public class AdminFreezeController {

    private final SemesterFreezeService freezeService;

    public AdminFreezeController(SemesterFreezeService freezeService) {
        this.freezeService = freezeService;
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FreezeResponse>> allRequests() {
        return ResponseEntity.ok(freezeService.getAllFreezeRequests());
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<FreezeResponse>> pending() {
        return ResponseEntity.ok(freezeService.getPendingFreezeRequests());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<FreezeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(freezeService.getFreezeById(id));
    }

    @PatchMapping("/requests/{id}/approve")
    public ResponseEntity<FreezeResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody FreezeApprovalRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                freezeService.approveFreeze(id, principal.getName(), request));
    }

    @PatchMapping("/requests/{id}/reject")
    public ResponseEntity<FreezeResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody FreezeApprovalRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                freezeService.rejectFreeze(id, principal.getName(), request));
    }

    @PatchMapping("/requests/{id}/approve-resume")
    public ResponseEntity<FreezeResponse> approveResume(
            @PathVariable Long id,
            @Valid @RequestBody FreezeApprovalRequest request,
            Principal principal) {
        return ResponseEntity.ok(
                freezeService.approveResume(id, principal.getName(), request));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<FreezeResponse>> studentHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(freezeService.getStudentFreezeHistory(studentId));
    }
}