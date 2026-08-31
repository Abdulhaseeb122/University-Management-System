package com.ums.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_evaluations",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_evaluation",
                columnNames = {"section_id", "student_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CourseEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "rating_teaching")
    private Integer ratingTeaching;

    @Column(name = "rating_course_content")
    private Integer ratingCourseContent;

    @Column(name = "rating_overall")
    private Integer ratingOverall;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;
}