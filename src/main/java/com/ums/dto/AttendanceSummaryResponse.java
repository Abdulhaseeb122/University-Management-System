package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceSummaryResponse {
    private Long sectionId;
    private String sectionName;
    private String courseCode;
    private String courseTitle;

    private long totalClasses;
    private long present;
    private long absent;
    private long late;
    private long excused;

    private double attendancePercentage; // (present + excused) / total * 100
    private boolean belowThreshold;      // true if < 75%
}