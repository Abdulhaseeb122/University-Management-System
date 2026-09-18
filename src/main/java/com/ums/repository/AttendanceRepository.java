package com.ums.repository;

import com.ums.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findBySectionIdAndDate(Long sectionId, LocalDate date);

    List<Attendance> findByStudentIdAndSectionId(Long studentId, Long sectionId);

    Optional<Attendance> findBySectionIdAndStudentIdAndDate(Long sectionId, Long studentId, LocalDate date);

    boolean existsBySectionIdAndStudentIdAndDate(Long sectionId, Long studentId, LocalDate date);

    // Count present/late days for a student in a section
    long countByStudentIdAndSectionIdAndStatusIn(
            Long studentId,
            Long sectionId,
            List<Attendance.AttendanceStatus> statuses);
}