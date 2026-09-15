package com.ums.controller;

import com.ums.dto.CourseSectionRequest;
import com.ums.dto.CourseSectionResponse;
import com.ums.service.CourseSectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/sections")
public class AdminCourseSectionController {

    private final CourseSectionService sectionService;

    public AdminCourseSectionController(CourseSectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping
    public ResponseEntity<CourseSectionResponse> createSection(@Valid @RequestBody CourseSectionRequest request) {
        return new ResponseEntity<>(sectionService.createSection(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CourseSectionResponse>> getAllSections() {
        return ResponseEntity.ok(sectionService.getAllSections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseSectionResponse> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseSectionResponse> updateSection(@PathVariable Long id,
                                                               @Valid @RequestBody CourseSectionRequest request) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/term/{termId}")
    public ResponseEntity<List<CourseSectionResponse>> getSectionsByTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(sectionService.getSectionsByTerm(termId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseSectionResponse>> getSectionsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(sectionService.getSectionsByCourse(courseId));
    }

    @GetMapping("/faculty/{facultyId}")
    public ResponseEntity<List<CourseSectionResponse>> getSectionsByFaculty(@PathVariable Long facultyId) {
        return ResponseEntity.ok(sectionService.getSectionsByFaculty(facultyId));
    }
}