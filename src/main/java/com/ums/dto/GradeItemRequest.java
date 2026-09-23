package com.ums.dto;

import com.ums.entity.GradeItem.ItemType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class GradeItemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100)
    private String title;

    @NotNull(message = "Item type is required")
    private ItemType itemType;

    @NotNull(message = "Max marks is required")
    @DecimalMin(value = "1.00", message = "Max marks must be at least 1")
    private BigDecimal maxMarks;

    @NotNull(message = "Weightage is required")
    @DecimalMin(value = "0.01", message = "Weightage must be greater than 0")
    @DecimalMax(value = "100.00", message = "Weightage cannot exceed 100")
    private BigDecimal weightagePercent;

    private LocalDateTime dueDate;
}