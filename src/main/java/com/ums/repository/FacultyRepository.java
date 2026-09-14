package com.ums.repository;

import com.ums.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmployeeId(String employeeId);
    Optional<Faculty> findByUserEmail(String email);
    boolean existsByEmployeeId(String employeeId);
    List<Faculty> findByDepartmentId(Long departmentId);
}