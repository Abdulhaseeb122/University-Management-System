package com.ums.repository;

import com.ums.entity.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicTermRepository extends JpaRepository<AcademicTerm, Long> {
    Optional<AcademicTerm> findByTermCode(String termCode);
    boolean existsByTermCode(String termCode);
    Optional<AcademicTerm> findByIsCurrentTrue();
}