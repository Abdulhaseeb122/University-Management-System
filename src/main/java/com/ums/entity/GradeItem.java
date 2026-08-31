package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "grade_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ItemType itemType;

    @Column(name = "max_marks", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxMarks;

    @Column(name = "weightage_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightagePercent;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    public enum ItemType {
        ASSIGNMENT, QUIZ, MIDTERM, FINAL_EXAM, PROJECT, PRESENTATION
    }
}