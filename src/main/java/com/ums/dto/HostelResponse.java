package com.ums.dto;

import com.ums.entity.Hostel.HostelType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostelResponse {
    private Long id;
    private String name;
    private HostelType type;
    private String wardenName;
    private String contactNumber;

    private Long campusId;
    private String campusName;

    // Derived
    private long totalRooms;
    private long totalCapacity;
    private long totalOccupied;
}