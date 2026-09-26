package com.ums.dto;

import com.ums.entity.StudentInvoice.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    private InvoiceStatus status;
    private LocalDateTime createdAt;

    // Student info
    private Long studentId;
    private String studentRollNumber;
    private String studentFullName;
    private String studentEmail;

    // Term info
    private Long termId;
    private String termName;
    private String termCode;
}