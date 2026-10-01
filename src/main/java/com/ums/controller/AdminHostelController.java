package com.ums.controller;

import com.ums.dto.*;
import com.ums.service.HostelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminHostelController {

    private final HostelService hostelService;

    public AdminHostelController(HostelService hostelService) {
        this.hostelService = hostelService;
    }

    // ---------- Hostels ----------

    @PostMapping("/hostels")
    public ResponseEntity<HostelResponse> createHostel(@Valid @RequestBody HostelRequest request) {
        return new ResponseEntity<>(hostelService.createHostel(request), HttpStatus.CREATED);
    }

    @GetMapping("/hostels")
    public ResponseEntity<List<HostelResponse>> getAllHostels() {
        return ResponseEntity.ok(hostelService.getAllHostels());
    }

    @GetMapping("/hostels/{id}")
    public ResponseEntity<HostelResponse> getHostel(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.getHostelById(id));
    }

    @PutMapping("/hostels/{id}")
    public ResponseEntity<HostelResponse> updateHostel(@PathVariable Long id,
                                                       @Valid @RequestBody HostelRequest request) {
        return ResponseEntity.ok(hostelService.updateHostel(id, request));
    }

    @DeleteMapping("/hostels/{id}")
    public ResponseEntity<Void> deleteHostel(@PathVariable Long id) {
        hostelService.deleteHostel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/hostels/{id}/occupancy")
    public ResponseEntity<HostelResponse> occupancy(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.getHostelOccupancy(id));
    }

    // ---------- Rooms ----------

    @PostMapping("/hostels/{hostelId}/rooms")
    public ResponseEntity<HostelRoomResponse> createRoom(@PathVariable Long hostelId,
                                                         @Valid @RequestBody HostelRoomRequest request) {
        return new ResponseEntity<>(hostelService.createRoom(hostelId, request), HttpStatus.CREATED);
    }

    @GetMapping("/hostels/{hostelId}/rooms")
    public ResponseEntity<List<HostelRoomResponse>> getRooms(@PathVariable Long hostelId) {
        return ResponseEntity.ok(hostelService.getRoomsByHostel(hostelId));
    }

    @PutMapping("/hostel-rooms/{roomId}")
    public ResponseEntity<HostelRoomResponse> updateRoom(@PathVariable Long roomId,
                                                         @Valid @RequestBody HostelRoomRequest request) {
        return ResponseEntity.ok(hostelService.updateRoom(roomId, request));
    }

    @DeleteMapping("/hostel-rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        hostelService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    // ---------- Allocations ----------

    @PostMapping("/hostel-allocations")
    public ResponseEntity<HostelAllocationResponse> allocate(@Valid @RequestBody HostelAllocationRequest request) {
        return new ResponseEntity<>(hostelService.allocateStudent(request), HttpStatus.CREATED);
    }

    @GetMapping("/hostel-allocations")
    public ResponseEntity<List<HostelAllocationResponse>> getAllAllocations() {
        return ResponseEntity.ok(hostelService.getAllActiveAllocations());
    }

    @PatchMapping("/hostel-allocations/{id}/vacate")
    public ResponseEntity<HostelAllocationResponse> vacate(@PathVariable Long id) {
        return ResponseEntity.ok(hostelService.vacateAllocation(id));
    }

    @GetMapping("/hostel-allocations/available-rooms")
    public ResponseEntity<List<HostelRoomResponse>> availableRooms() {
        return ResponseEntity.ok(hostelService.getAvailableRooms());
    }
}