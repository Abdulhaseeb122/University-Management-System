package com.ums.repository;

import com.ums.entity.ExamSeating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSeatingRepository extends JpaRepository<ExamSeating, Long> {

    List<ExamSeating> findByExamId(Long examId);

    List<ExamSeating> findByExamIdOrderBySeatNumberAsc(Long examId);

    Optional<ExamSeating> findByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamId(Long examId);

    long countByExamId(Long examId);

    // Check if a seat number is already taken in an exam
    boolean existsByExamIdAndSeatNumber(Long examId, String seatNumber);

    // Delete all seating for an exam (for re-allocation)
    @Modifying
    @Query("DELETE FROM ExamSeating es WHERE es.exam.id = :examId")
    void deleteAllByExamId(@Param("examId") Long examId);

    // All exams I'm invigilating
    List<ExamSeating> findByInvigilatorId(Long invigilatorId);
}