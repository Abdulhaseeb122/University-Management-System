package com.ums.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignInvigilatorRequest {

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;
}