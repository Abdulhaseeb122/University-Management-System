package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "final_course_grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalCourseGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "letter_grade", length = 5)
    private String letterGrade; // e.g., "A", "B+", "C", "F"

    @Column(name = "grade_point", precision = 3, scale = 2)
    private BigDecimal gradePoint; // e.g., 4.00, 3.50

    @Column(name = "is_published")
    private Boolean isPublished = false;
}