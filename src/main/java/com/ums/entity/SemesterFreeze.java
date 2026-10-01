package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "semester_freezes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SemesterFreeze {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeze_from_term_id", nullable = false)
    private AcademicTerm freezeFromTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expected_return_term_id")
    private AcademicTerm expectedReturnTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actual_return_term_id")
    private AcademicTerm actualReturnTerm;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FreezeStatus status = FreezeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "admin_remarks", length = 500)
    private String adminRemarks;

    @Column(name = "resumed_at")
    private LocalDateTime resumedAt;

    public enum FreezeStatus {
        PENDING,
        APPROVED,
        REJECTED,
        RESUMED,
        CANCELLED
    }
}