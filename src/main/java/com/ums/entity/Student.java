package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "roll_number", nullable = false, unique = true, length = 20)
    private String rollNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    @Column(name = "current_semester")
    @Builder.Default
    private Integer currentSemester = 1;

    @Column(name = "admission_date", nullable = false)
    @Builder.Default
    private LocalDate admissionDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status")
    @Builder.Default
    private AcademicStatus academicStatus = AcademicStatus.ACTIVE;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal cgpa = BigDecimal.ZERO;

    @Column(name = "total_credits_earned")
    @Builder.Default
    private Integer totalCreditsEarned = 0;

    public enum AcademicStatus {
        ACTIVE, PROBATION, SUSPENDED, GRADUATED, WITHDRAWN
    }
}