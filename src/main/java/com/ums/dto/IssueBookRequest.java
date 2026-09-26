package com.ums.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueBookRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "User ID is required")
    private Long userId;
}