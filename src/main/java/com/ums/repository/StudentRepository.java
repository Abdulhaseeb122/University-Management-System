package com.ums.repository;

import com.ums.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Boolean existsByRollNumber(String rollNumber);
    List<Student> findByDepartmentId(Long departmentId);

    // --- NEW METHOD ---
    Optional<Student> findByUserEmail(String email); // Find student by user's email
}