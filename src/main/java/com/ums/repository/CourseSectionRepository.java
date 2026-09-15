package com.ums.repository;

import com.ums.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    boolean existsByCourseId(Long courseId);
    boolean existsByTermId(Long termId);
    boolean existsByFacultyId(Long facultyId);

    List<CourseSection> findByTermId(Long termId);
    List<CourseSection> findByCourseId(Long courseId);
    List<CourseSection> findByFacultyId(Long facultyId);

    // Uniqueness check: same section name cannot repeat for the same course in the same term
    boolean existsByCourseIdAndTermIdAndSectionName(Long courseId, Long termId, String sectionName);
}