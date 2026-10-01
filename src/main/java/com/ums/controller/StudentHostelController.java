package com.ums.controller;

import com.ums.dto.HostelAllocationResponse;
import com.ums.dto.HostelRoomResponse;
import com.ums.service.HostelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/hostel")
public class StudentHostelController {

    private final HostelService hostelService;

    public StudentHostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    @GetMapping("/my-allocation")
    public ResponseEntity<HostelAllocationResponse> myAllocation(Principal principal) {
        return ResponseEntity.ok(hostelService.getMyAllocation(principal.getName()));
    }

    @GetMapping("/available")
    public ResponseEntity<List<HostelRoomResponse>> availableForMe(Principal principal) {
        return ResponseEntity.ok(hostelService.getAvailableRoomsForMe(principal.getName()));
    }
}