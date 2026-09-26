package com.ums.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String targetRole;
    private LocalDateTime createdAt;

    // Who created it
    private Long createdById;
    private String createdByName;
}