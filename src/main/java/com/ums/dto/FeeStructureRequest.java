package com.ums.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FeeStructureRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Tuition fee per credit is required")
    @DecimalMin(value = "0.00", message = "Tuition fee cannot be negative")
    private BigDecimal tuitionFeePerCredit;

    @DecimalMin(value = "0.00", message = "Library fee cannot be negative")
    private BigDecimal libraryFee;

    @DecimalMin(value = "0.00", message = "Lab fee cannot be negative")
    private BigDecimal labFee;

    @DecimalMin(value = "0.00", message = "Hostel fee cannot be negative")
    private BigDecimal hostelFee;
}