package com.ums.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreezeApplicationRequest {

    @NotNull(message = "Freeze-from term ID is required")
    private Long freezeFromTermId;

    @NotNull(message = "Expected return term ID is required")
    private Long expectedReturnTermId;

    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}