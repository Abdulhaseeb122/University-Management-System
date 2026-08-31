package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_structures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private AcademicTerm term;

    @Column(name = "tuition_fee_per_credit", nullable = false, precision = 10, scale = 2)
    private BigDecimal tuitionFeePerCredit;

    @Column(name = "library_fee", precision = 10, scale = 2)
    private BigDecimal libraryFee = BigDecimal.ZERO;

    @Column(name = "lab_fee", precision = 10, scale = 2)
    private BigDecimal labFee = BigDecimal.ZERO;

    @Column(name = "hostel_fee", precision = 10, scale = 2)
    private BigDecimal hostelFee = BigDecimal.ZERO;
}