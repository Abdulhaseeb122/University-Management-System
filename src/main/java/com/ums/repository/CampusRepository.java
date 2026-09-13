package com.ums.repository;

import com.ums.entity.Campus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    Optional<Campus> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}