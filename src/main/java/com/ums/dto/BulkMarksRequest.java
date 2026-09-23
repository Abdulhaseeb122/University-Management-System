package com.ums.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkMarksRequest {

    @NotEmpty(message = "Marks list cannot be empty")
    @Valid
    private List<MarksRecord> marks;
}