package com.ums.repository;

import com.ums.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByTermId(Long termId);

    List<Exam> findByCourseId(Long courseId);

    List<Exam> findByExamDateAfterOrderByExamDateAscStartTimeAsc(LocalDate date);

    // Check room double-booking: same room, same date, overlapping times
    @Query("SELECT e FROM Exam e WHERE e.room.id = :roomId " +
            "AND e.examDate = :examDate " +
            "AND ((e.startTime < :endTime AND e.endTime > :startTime))")
    List<Exam> findOverlappingExams(
            @Param("roomId") Long roomId,
            @Param("examDate") LocalDate examDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}