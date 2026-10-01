package com.ums.dto;

import com.ums.entity.SemesterFreeze.FreezeStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FreezeResponse {
    private Long id;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    // Terms
    private Long freezeFromTermId;
    private String freezeFromTermName;
    private Long expectedReturnTermId;
    private String expectedReturnTermName;
    private Long actualReturnTermId;
    private String actualReturnTermName;

    private String reason;
    private FreezeStatus status;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime resumedAt;

    private Long approvedById;
    private String approvedByName;
    private String adminRemarks;
}