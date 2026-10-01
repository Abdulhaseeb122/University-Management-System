package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HostelRoomResponse {
    private Long id;
    private String roomNumber;
    private Integer capacity;
    private BigDecimal monthlyRent;

    private Long hostelId;
    private String hostelName;
    private String hostelType;
    private String campusName;

    // Derived
    private long occupiedBeds;
    private long availableBeds;
}