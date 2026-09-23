package com.ums.repository;

import com.ums.entity.FinalCourseGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinalCourseGradeRepository extends JpaRepository<FinalCourseGrade, Long> {

    Optional<FinalCourseGrade> findByEnrollmentId(Long enrollmentId);

    List<FinalCourseGrade> findByEnrollmentSectionId(Long sectionId);

    List<FinalCourseGrade> findByEnrollmentStudentId(Long studentId);

    // Only published grades (for student view)
    List<FinalCourseGrade> findByEnrollmentStudentIdAndIsPublishedTrue(Long studentId);

    boolean existsByEnrollmentId(Long enrollmentId);
}