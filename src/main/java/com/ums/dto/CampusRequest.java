package com.ums.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CampusRequest {

    @NotBlank(message = "Campus name is required")
    @Size(max = 100, message = "Campus name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Campus code is required")
    @Size(max = 10, message = "Campus code cannot exceed 10 characters")
    private String code;

    @NotBlank(message = "Address is required")
    private String address;

    @Email(message = "Invalid email format")
    private String contactEmail;
}