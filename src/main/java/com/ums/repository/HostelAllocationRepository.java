package com.ums.repository;

import com.ums.entity.HostelAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HostelAllocationRepository extends JpaRepository<HostelAllocation, Long> {

    // The active (not vacated) allocation of a student
    Optional<HostelAllocation> findByStudentIdAndVacatedDateIsNull(Long studentId);

    // All allocations for a room (active + historical)
    List<HostelAllocation> findByHostelRoomId(Long hostelRoomId);

    // Active allocations in a room
    List<HostelAllocation> findByHostelRoomIdAndVacatedDateIsNull(Long hostelRoomId);

    // Count active occupants in a room
    long countByHostelRoomIdAndVacatedDateIsNull(Long hostelRoomId);

    // Check if a student already has an active allocation
    boolean existsByStudentIdAndVacatedDateIsNull(Long studentId);

    // All active allocations
    List<HostelAllocation> findByVacatedDateIsNull();

    // Check if a room has any active allocations
    boolean existsByHostelRoomIdAndVacatedDateIsNull(Long hostelRoomId);

    // All hostels with active allocations
    List<HostelAllocation> findByHostelRoomHostelIdAndVacatedDateIsNull(Long hostelId);
}