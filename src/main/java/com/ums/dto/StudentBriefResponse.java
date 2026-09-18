package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentBriefResponse {
    private Long id;
    private String rollNumber;
    private String fullName;
    private String email;
}