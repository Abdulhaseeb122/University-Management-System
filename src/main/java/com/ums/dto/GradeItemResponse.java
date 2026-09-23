package com.ums.dto;

import com.ums.entity.GradeItem.ItemType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GradeItemResponse {
    private Long id;
    private String title;
    private ItemType itemType;
    private BigDecimal maxMarks;
    private BigDecimal weightagePercent;
    private LocalDateTime dueDate;

    private Long sectionId;
    private String sectionName;
    private String courseCode;
}