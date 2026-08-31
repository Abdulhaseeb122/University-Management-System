package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false, unique = true, length = 15)
    private String code; // e.g., CS101

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "lecture_hours", nullable = false)
    private Integer lectureHours = 3;

    @Column(name = "lab_hours", nullable = false)
    private Integer labHours = 0;

    @Column(nullable = false)
    private Integer credits;

    @Column(name = "is_elective")
    private Boolean isElective = false;
}