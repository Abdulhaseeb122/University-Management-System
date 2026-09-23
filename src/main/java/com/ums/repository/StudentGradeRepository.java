package com.ums.repository;

import com.ums.entity.StudentGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, Long> {

    List<StudentGrade> findByGradeItemId(Long gradeItemId);

    Optional<StudentGrade> findByGradeItemIdAndStudentId(Long gradeItemId, Long studentId);

    boolean existsByGradeItemId(Long gradeItemId);

    // All marks a student earned in a specific section (via grade item)
    List<StudentGrade> findByStudentIdAndGradeItemSectionId(Long studentId, Long sectionId);

    // All marks for a student across all sections (for transcript)
    List<StudentGrade> findByStudentId(Long studentId);
}