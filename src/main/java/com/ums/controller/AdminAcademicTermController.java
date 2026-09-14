package com.ums.controller;

import com.ums.dto.AcademicTermRequest;
import com.ums.dto.AcademicTermResponse;
import com.ums.service.AcademicTermService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/terms")
public class AdminAcademicTermController {

    private final AcademicTermService termService;

    public AdminAcademicTermController(AcademicTermService termService) {
        this.termService = termService;
    }

    @PostMapping
    public ResponseEntity<AcademicTermResponse> createTerm(@Valid @RequestBody AcademicTermRequest request) {
        return new ResponseEntity<>(termService.createTerm(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AcademicTermResponse>> getAllTerms() {
        return ResponseEntity.ok(termService.getAllTerms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicTermResponse> getTermById(@PathVariable Long id) {
        return ResponseEntity.ok(termService.getTermById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicTermResponse> updateTerm(@PathVariable Long id,
                                                           @Valid @RequestBody AcademicTermRequest request) {
        return ResponseEntity.ok(termService.updateTerm(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerm(@PathVariable Long id) {
        termService.deleteTerm(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/set-current")
    public ResponseEntity<AcademicTermResponse> setCurrentTerm(@PathVariable Long id) {
        return ResponseEntity.ok(termService.setCurrentTerm(id));
    }
}