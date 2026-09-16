package com.ums.repository;

import com.ums.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findBySectionId(Long sectionId);

    // Existing check — used to prevent duplicate enrollment
    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);

    // Find a specific enrollment by student and section
    Optional<Enrollment> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    // Check if student has any active enrollment in a specific section
    boolean existsByStudentIdAndSectionIdAndStatus(Long studentId, Long sectionId, Enrollment.EnrollmentStatus status);
}