package com.ums.controller;

import com.ums.dto.CampusRequest;
import com.ums.dto.CampusResponse;
import com.ums.service.CampusService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/campuses")
public class AdminCampusController {

    private final CampusService campusService;

    public AdminCampusController(CampusService campusService) {
        this.campusService = campusService;
    }

    @PostMapping
    public ResponseEntity<CampusResponse> createCampus(@Valid @RequestBody CampusRequest request) {
        return new ResponseEntity<>(campusService.createCampus(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CampusResponse>> getAllCampuses() {
        return ResponseEntity.ok(campusService.getAllCampuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusResponse> getCampusById(@PathVariable Long id) {
        return ResponseEntity.ok(campusService.getCampusById(id));
    }
}