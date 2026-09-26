package com.ums.controller;

import com.ums.dto.AnnouncementRequest;
import com.ums.dto.AnnouncementResponse;
import com.ums.service.AnnouncementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    public AdminAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody AnnouncementRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                announcementService.createAnnouncement(principal.getName(), request),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> getAll() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}