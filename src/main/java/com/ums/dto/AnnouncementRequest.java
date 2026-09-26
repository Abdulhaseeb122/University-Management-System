package com.ums.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    // "ALL" | "ROLE_STUDENT" | "ROLE_FACULTY" | "ROLE_ADMIN"
    @Pattern(
            regexp = "^(ALL|ROLE_STUDENT|ROLE_FACULTY|ROLE_ADMIN)$",
            message = "targetRole must be one of: ALL, ROLE_STUDENT, ROLE_FACULTY, ROLE_ADMIN"
    )
    private String targetRole = "ALL";
}