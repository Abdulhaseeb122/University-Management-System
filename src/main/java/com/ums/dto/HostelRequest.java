package com.ums.dto;

import com.ums.entity.Hostel.HostelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostelRequest {

    @NotNull(message = "Campus ID is required")
    private Long campusId;

    @NotBlank(message = "Hostel name is required")
    @Size(max = 100, message = "Hostel name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Hostel type is required")
    private HostelType type;

    @Size(max = 100)
    private String wardenName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String contactNumber;
}