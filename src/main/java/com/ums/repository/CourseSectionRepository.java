package com.ums.repository;
import com.ums.entity.CourseSection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    Optional<CourseSection> findById(Long id);
}