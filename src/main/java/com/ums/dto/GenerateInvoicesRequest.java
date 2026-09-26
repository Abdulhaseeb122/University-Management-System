package com.ums.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GenerateInvoicesRequest {

    @NotNull(message = "Term ID is required")
    private Long termId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}