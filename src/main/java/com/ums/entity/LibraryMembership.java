package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "library_memberships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one with User (a user can have only one active membership)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "library_card_number", unique = true, nullable = false, length = 30)
    private String libraryCardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false, length = 20)
    private MembershipType membershipType;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    public enum MembershipType {
        STUDENT, FACULTY
    }

    public enum MembershipStatus {
        ACTIVE, SUSPENDED, EXPIRED
    }
}