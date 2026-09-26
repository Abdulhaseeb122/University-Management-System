package com.ums.dto;

import com.ums.entity.LibraryMembership.MembershipStatus;
import com.ums.entity.LibraryMembership.MembershipType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MembershipResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;

    private String libraryCardNumber;
    private MembershipType membershipType;
    private String contactPhone;
    private String address;
    private LocalDate validUntil;
    private MembershipStatus status;
    private LocalDateTime registeredAt;

    // Derived — true if status=ACTIVE AND validUntil >= today
    private boolean valid;
}