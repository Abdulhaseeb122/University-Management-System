package com.ums.repository;

import com.ums.entity.SemesterFreeze;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SemesterFreezeRepository extends JpaRepository<SemesterFreeze, Long> {

    List<SemesterFreeze> findByStudentIdOrderByRequestedAtDesc(Long studentId);

    Optional<SemesterFreeze> findFirstByStudentIdAndStatusOrderByRequestedAtDesc(
            Long studentId, SemesterFreeze.FreezeStatus status);

    boolean existsByStudentIdAndStatus(Long studentId, SemesterFreeze.FreezeStatus status);

    List<SemesterFreeze> findByStatusOrderByRequestedAtAsc(SemesterFreeze.FreezeStatus status);

    List<SemesterFreeze> findAllByOrderByRequestedAtDesc();

    // Active freeze (status = APPROVED and not yet resumed) for a student
    Optional<SemesterFreeze> findFirstByStudentIdAndStatusIn(
            Long studentId, List<SemesterFreeze.FreezeStatus> statuses);

    Optional<SemesterFreeze> findFirstByStudentIdAndStatus(Long studentId,
                                                           SemesterFreeze.FreezeStatus status);
}