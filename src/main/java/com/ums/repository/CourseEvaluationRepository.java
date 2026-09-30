package com.ums.repository;

import com.ums.entity.CourseEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseEvaluationRepository extends JpaRepository<CourseEvaluation, Long> {

    // Check if a student already evaluated a section
    boolean existsBySectionIdAndStudentId(Long sectionId, Long studentId);

    Optional<CourseEvaluation> findBySectionIdAndStudentId(Long sectionId, Long studentId);

    // All evaluations for a section
    List<CourseEvaluation> findBySectionId(Long sectionId);

    // All evaluations written by a student
    List<CourseEvaluation> findByStudentId(Long studentId);

    // All evaluations for sections taught by a faculty member
    @Query("SELECT e FROM CourseEvaluation e WHERE e.section.faculty.id = :facultyId")
    List<CourseEvaluation> findByFacultyId(@Param("facultyId") Long facultyId);

    // All evaluations for sections of a course
    @Query("SELECT e FROM CourseEvaluation e WHERE e.section.course.id = :courseId")
    List<CourseEvaluation> findByCourseId(@Param("courseId") Long courseId);

    // Aggregated averages for a faculty member
    @Query("SELECT AVG(e.ratingTeaching), AVG(e.ratingCourseContent), AVG(e.ratingOverall), COUNT(e) " +
            "FROM CourseEvaluation e WHERE e.section.faculty.id = :facultyId")
    Object[] getFacultyAverages(@Param("facultyId") Long facultyId);

    // Aggregated averages for a section
    @Query("SELECT AVG(e.ratingTeaching), AVG(e.ratingCourseContent), AVG(e.ratingOverall), COUNT(e) " +
            "FROM CourseEvaluation e WHERE e.section.id = :sectionId")
    Object[] getSectionAverages(@Param("sectionId") Long sectionId);
}