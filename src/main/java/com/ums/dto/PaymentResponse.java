package com.ums.dto;

import com.ums.entity.Payment.PaymentMethod;
import com.ums.entity.Payment.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long invoiceId;
    private String invoiceNumber;
    private String transactionReference;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime paymentDate;

    // Student info
    private Long studentId;
    private String studentFullName;
}
