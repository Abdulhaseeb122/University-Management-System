package com.ums.repository;
import com.ums.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentIdAndSectionId(Long studentId, Long sectionId);
    List<Enrollment> findByStudentId(Long studentId);
}