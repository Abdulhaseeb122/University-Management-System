package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StudentFinanceSummaryResponse {
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;

    private int totalInvoices;
    private BigDecimal totalBilled;
    private BigDecimal totalPaid;
    private BigDecimal totalOutstanding;
    private long overdueCount;
    private long paidCount;
}