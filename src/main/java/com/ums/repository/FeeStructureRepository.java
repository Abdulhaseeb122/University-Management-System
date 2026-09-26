package com.ums.repository;

import com.ums.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    // Uniqueness: one fee structure per department per term
    boolean existsByDepartmentIdAndTermId(Long departmentId, Long termId);

    Optional<FeeStructure> findByDepartmentIdAndTermId(Long departmentId, Long termId);

    List<FeeStructure> findByTermId(Long termId);

    List<FeeStructure> findByDepartmentId(Long departmentId);
}