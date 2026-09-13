package com.ums.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CampusResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String contactEmail;
}