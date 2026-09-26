package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FeeStructureResponse {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Long termId;
    private String termName;
    private String termCode;

    private BigDecimal tuitionFeePerCredit;
    private BigDecimal libraryFee;
    private BigDecimal labFee;
    private BigDecimal hostelFee;

    // Computed: total for a sample student with N credits
    // Not used here — just for reference in the frontend
}