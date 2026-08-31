package com.ums.repository;

import com.ums.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCode(String code); // e.g., "CS101"[cite: 1]
    Boolean existsByCode(String code);
    List<Course> findByDepartmentId(Long departmentId);
}