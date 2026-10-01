package com.ums.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreezeApprovalRequest {

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}