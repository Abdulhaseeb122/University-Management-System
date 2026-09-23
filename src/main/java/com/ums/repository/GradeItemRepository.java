package com.ums.repository;

import com.ums.entity.GradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface GradeItemRepository extends JpaRepository<GradeItem, Long> {

    List<GradeItem> findBySectionId(Long sectionId);

    // Check if sum of weightages would exceed 100
    @Query("SELECT COALESCE(SUM(g.weightagePercent), 0) FROM GradeItem g WHERE g.section.id = :sectionId")
    BigDecimal sumWeightageBySectionId(@Param("sectionId") Long sectionId);
}