package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class BookIssueResponse {
    private Long id;

    // Book info
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private String bookAuthor;

    // User info
    private Long userId;
    private String userFullName;
    private String userEmail;

    // Dates
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    // Money
    private BigDecimal fineAmount;

    // Derived
    private boolean overdue;
}