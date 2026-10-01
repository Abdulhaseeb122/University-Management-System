package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class HostelAllocationResponse {
    private Long id;
    private LocalDate allocatedDate;
    private LocalDate vacatedDate;
    private BigDecimal monthlyRentSnapshot;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Room info
    private Long roomId;
    private String roomNumber;
    private Integer roomCapacity;

    // Hostel info
    private Long hostelId;
    private String hostelName;
    private String hostelType;

    // Campus
    private String campusName;

    // Derived
    private boolean active;
}