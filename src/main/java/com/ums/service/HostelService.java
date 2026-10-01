package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface HostelService {

    // Admin - Hostels
    HostelResponse createHostel(HostelRequest request);
    List<HostelResponse> getAllHostels();
    HostelResponse getHostelById(Long id);
    HostelResponse updateHostel(Long id, HostelRequest request);
    void deleteHostel(Long id);

    // Admin - Rooms
    HostelRoomResponse createRoom(Long hostelId, HostelRoomRequest request);
    List<HostelRoomResponse> getRoomsByHostel(Long hostelId);
    HostelRoomResponse updateRoom(Long roomId, HostelRoomRequest request);
    void deleteRoom(Long roomId);

    // Admin - Allocations
    HostelAllocationResponse allocateStudent(HostelAllocationRequest request);
    List<HostelAllocationResponse> getAllActiveAllocations();
    HostelAllocationResponse vacateAllocation(Long allocationId);
    List<HostelRoomResponse> getAvailableRooms();
    HostelResponse getHostelOccupancy(Long hostelId);

    // Student
    HostelAllocationResponse getMyAllocation(String studentEmail);
    List<HostelRoomResponse> getAvailableRoomsForMe(String studentEmail);
}