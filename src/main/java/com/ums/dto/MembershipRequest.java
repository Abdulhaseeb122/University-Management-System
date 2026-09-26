package com.ums.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembershipRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Library card number is required")
    @Size(max = 30, message = "Card number cannot exceed 30 characters")
    private String libraryCardNumber;

    // NOTE: membershipType is AUTO-DERIVED from user's role — not accepted from client.

    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String contactPhone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Valid until date is required")
    @Future(message = "Validity date must be in the future")
    private LocalDate validUntil;
}