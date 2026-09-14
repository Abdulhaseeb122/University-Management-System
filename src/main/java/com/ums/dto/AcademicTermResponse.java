package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AcademicTermResponse {
    private Long id;
    private String name;
    private String termCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
}