package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.HostelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final HostelRoomRepository roomRepository;
    private final HostelAllocationRepository allocationRepository;
    private final CampusRepository campusRepository;
    private final StudentRepository studentRepository;

    public HostelServiceImpl(HostelRepository hostelRepository,
                             HostelRoomRepository roomRepository,
                             HostelAllocationRepository allocationRepository,
                             CampusRepository campusRepository,
                             StudentRepository studentRepository) {
        this.hostelRepository = hostelRepository;
        this.roomRepository = roomRepository;
        this.allocationRepository = allocationRepository;
        this.campusRepository = campusRepository;
        this.studentRepository = studentRepository;
    }

    // ================================================================
    // ADMIN: Hostel CRUD
    // ================================================================
    @Override
    @Transactional
    public HostelResponse createHostel(HostelRequest request) {
        Campus campus = campusRepository.findById(request.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campus not found: " + request.getCampusId()));

        Hostel hostel = new Hostel();
        hostel.setCampus(campus);
        hostel.setName(request.getName());
        hostel.setType(request.getType());
        hostel.setWardenName(request.getWardenName());
        hostel.setContactNumber(request.getContactNumber());

        return mapHostelToResponse(hostelRepository.save(hostel));
    }

    @Override
    public List<HostelResponse> getAllHostels() {
        return hostelRepository.findAll().stream()
                .map(this::mapHostelToResponse).collect(Collectors.toList());
    }

    @Override
    public HostelResponse getHostelById(Long id) {
        Hostel h = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found: " + id));
        return mapHostelToResponse(h);
    }

    @Override
    @Transactional
    public HostelResponse updateHostel(Long id, HostelRequest request) {
        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found: " + id));

        Campus campus = campusRepository.findById(request.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Campus not found: " + request.getCampusId()));

        hostel.setCampus(campus);
        hostel.setName(request.getName());
        hostel.setType(request.getType());
        hostel.setWardenName(request.getWardenName());
        hostel.setContactNumber(request.getContactNumber());

        return mapHostelToResponse(hostelRepository.save(hostel));
    }

    @Override
    @Transactional
    public void deleteHostel(Long id) {
        Hostel hostel = hostelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found: " + id));

        // Safety: cannot delete if any active allocation exists
        long activeAllocs = allocationRepository
                .findByHostelRoomHostelIdAndVacatedDateIsNull(id).size();
        if (activeAllocs > 0) {
            throw new BadRequestException(
                    "Cannot delete hostel: " + activeAllocs + " active allocation(s) exist.");
        }

        // Also block if rooms exist (ask admin to remove rooms first)
        if (roomRepository.existsByHostelId(id)) {
            throw new BadRequestException(
                    "Cannot delete hostel: remove all rooms first.");
        }

        hostelRepository.delete(hostel);
    }

    // ================================================================
    // ADMIN: Room CRUD
    // ================================================================
    @Override
    @Transactional
    public HostelRoomResponse createRoom(Long hostelId, HostelRoomRequest request) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found: " + hostelId));

        HostelRoom room = new HostelRoom();
        room.setHostel(hostel);
        room.setRoomNumber(request.getRoomNumber());
        room.setCapacity(request.getCapacity());
        room.setMonthlyRent(request.getMonthlyRent());

        return mapRoomToResponse(roomRepository.save(room));
    }

    @Override
    public List<HostelRoomResponse> getRoomsByHostel(Long hostelId) {
        if (!hostelRepository.existsById(hostelId)) {
            throw new ResourceNotFoundException("Hostel not found: " + hostelId);
        }
        return roomRepository.findByHostelId(hostelId).stream()
                .map(this::mapRoomToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HostelRoomResponse updateRoom(Long roomId, HostelRoomRequest request) {
        HostelRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        // Cannot reduce capacity below current occupancy
        long occupied = allocationRepository.countByHostelRoomIdAndVacatedDateIsNull(roomId);
        if (request.getCapacity() < occupied) {
            throw new BadRequestException(
                    "Cannot reduce capacity below current occupancy (" + occupied + ").");
        }

        room.setRoomNumber(request.getRoomNumber());
        room.setCapacity(request.getCapacity());
        room.setMonthlyRent(request.getMonthlyRent());

        return mapRoomToResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        HostelRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));

        if (allocationRepository.existsByHostelRoomIdAndVacatedDateIsNull(roomId)) {
            throw new BadRequestException(
                    "Cannot delete room: active allocations exist.");
        }

        roomRepository.delete(room);
    }

    // ================================================================
    // ADMIN: Allocate student
    // ================================================================
    @Override
    @Transactional
    public HostelAllocationResponse allocateStudent(HostelAllocationRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found: " + request.getStudentId()));

        HostelRoom room = roomRepository.findById(request.getHostelRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found: " + request.getHostelRoomId()));

        // 1. Student must not already have an active allocation
        if (allocationRepository.existsByStudentIdAndVacatedDateIsNull(student.getId())) {
            throw new BadRequestException("Student already has an active hostel allocation.");
        }

        // 2. Room must have space
        long occupied = allocationRepository.countByHostelRoomIdAndVacatedDateIsNull(room.getId());
        if (occupied >= room.getCapacity()) {
            throw new BadRequestException("Room is full (" + occupied + "/" + room.getCapacity() + ").");
        }

        // 3. Gender match: Male student → MALE or COED; Female student → FEMALE or COED
        Hostel hostel = room.getHostel();
        String studentGender = student.getUser().getGender().name();  // MALE / FEMALE / OTHER
        Hostel.HostelType hostelType = hostel.getType();

        if ("MALE".equals(studentGender)
                && hostelType == Hostel.HostelType.FEMALE) {
            throw new BadRequestException("Male students cannot be allocated to a female-only hostel.");
        }
        if ("FEMALE".equals(studentGender)
                && hostelType == Hostel.HostelType.MALE) {
            throw new BadRequestException("Female students cannot be allocated to a male-only hostel.");
        }

        // 4. Create allocation
        HostelAllocation allocation = new HostelAllocation();
        allocation.setStudent(student);
        allocation.setHostelRoom(room);
        allocation.setAllocatedDate(LocalDate.now());
        // Note: your entity doesn't have a monthlyRentSnapshot field.
        // If you want the snapshot, add it to the entity. For now, we skip it.

        return mapAllocationToResponse(allocationRepository.save(allocation));
    }

    @Override
    public List<HostelAllocationResponse> getAllActiveAllocations() {
        return allocationRepository.findByVacatedDateIsNull().stream()
                .map(this::mapAllocationToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HostelAllocationResponse vacateAllocation(Long allocationId) {
        HostelAllocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found: " + allocationId));

        if (allocation.getVacatedDate() != null) {
            throw new BadRequestException("Allocation is already vacated.");
        }

        allocation.setVacatedDate(LocalDate.now());
        return mapAllocationToResponse(allocationRepository.save(allocation));
    }

    @Override
    public List<HostelRoomResponse> getAvailableRooms() {
        return roomRepository.findAll().stream()
                .filter(r -> allocationRepository
                        .countByHostelRoomIdAndVacatedDateIsNull(r.getId()) < r.getCapacity())
                .map(this::mapRoomToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public HostelResponse getHostelOccupancy(Long hostelId) {
        Hostel hostel = hostelRepository.findById(hostelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found: " + hostelId));

        return mapHostelToResponse(hostel);
    }

    // ================================================================
    // STUDENT
    // ================================================================
    @Override
    public HostelAllocationResponse getMyAllocation(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        HostelAllocation allocation = allocationRepository
                .findByStudentIdAndVacatedDateIsNull(student.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You don't have an active hostel allocation."));

        return mapAllocationToResponse(allocation);
    }

    @Override
    public List<HostelRoomResponse> getAvailableRoomsForMe(String studentEmail) {
        Student student = studentRepository.findByUserEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentEmail));

        String studentGender = student.getUser().getGender().name();

        return roomRepository.findAll().stream()
                // Only rooms with space
                .filter(r -> allocationRepository
                        .countByHostelRoomIdAndVacatedDateIsNull(r.getId()) < r.getCapacity())
                // Gender-appropriate
                .filter(r -> {
                    Hostel.HostelType t = r.getHostel().getType();
                    if ("MALE".equals(studentGender) && t == Hostel.HostelType.FEMALE) return false;
                    if ("FEMALE".equals(studentGender) && t == Hostel.HostelType.MALE) return false;
                    return true;
                })
                .map(this::mapRoomToResponse)
                .collect(Collectors.toList());
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private HostelResponse mapHostelToResponse(Hostel hostel) {
        List<HostelRoom> rooms = roomRepository.findByHostelId(hostel.getId());

        long totalCapacity = rooms.stream()
                .mapToLong(HostelRoom::getCapacity)
                .sum();

        long totalOccupied = rooms.stream()
                .mapToLong(r -> allocationRepository.countByHostelRoomIdAndVacatedDateIsNull(r.getId()))
                .sum();

        return HostelResponse.builder()
                .id(hostel.getId())
                .name(hostel.getName())
                .type(hostel.getType())
                .wardenName(hostel.getWardenName())
                .contactNumber(hostel.getContactNumber())
                .campusId(hostel.getCampus().getId())
                .campusName(hostel.getCampus().getName())
                .totalRooms(rooms.size())
                .totalCapacity(totalCapacity)
                .totalOccupied(totalOccupied)
                .build();
    }

    private HostelRoomResponse mapRoomToResponse(HostelRoom room) {
        long occupied = allocationRepository
                .countByHostelRoomIdAndVacatedDateIsNull(room.getId());

        Hostel hostel = room.getHostel();
        return HostelRoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .capacity(room.getCapacity())
                .monthlyRent(room.getMonthlyRent())
                .hostelId(hostel.getId())
                .hostelName(hostel.getName())
                .hostelType(hostel.getType().name())
                .campusName(hostel.getCampus().getName())
                .occupiedBeds(occupied)
                .availableBeds(room.getCapacity() - occupied)
                .build();
    }

    private HostelAllocationResponse mapAllocationToResponse(HostelAllocation a) {
        Student student = a.getStudent();
        HostelRoom room = a.getHostelRoom();
        Hostel hostel = room.getHostel();

        return HostelAllocationResponse.builder()
                .id(a.getId())
                .allocatedDate(a.getAllocatedDate())
                .vacatedDate(a.getVacatedDate())
                .studentId(student.getId())
                .studentRollNumber(student.getRollNumber())
                .studentFullName(student.getUser().getFirstName() + " "
                        + student.getUser().getLastName())
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomCapacity(room.getCapacity())
                .hostelId(hostel.getId())
                .hostelName(hostel.getName())
                .hostelType(hostel.getType().name())
                .campusName(hostel.getCampus().getName())
                .active(a.getVacatedDate() == null)
                .build();
    }
}