package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_seating")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ExamSeating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "seat_number", nullable = false, length = 20)
    private String seatNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invigilator_faculty_id")
    private Faculty invigilator;
}