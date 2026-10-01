package com.ums.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeRequest {

    @NotNull(message = "Return term ID is required")
    private Long returnTermId;
}