package com.ums.service;

import com.ums.dto.AttendanceResponse;
import com.ums.dto.AttendanceSummaryResponse;
import com.ums.dto.BulkAttendanceRequest;
import com.ums.dto.CourseSectionResponse;
import com.ums.dto.StudentBriefResponse;   // <-- YEH IMPORT MISSING THA

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    // Faculty endpoints
    List<CourseSectionResponse> getMySections(String facultyEmail);

    List<StudentBriefResponse> getEnrolledStudents(Long sectionId, String facultyEmail);

    List<AttendanceResponse> markBulkAttendance(Long sectionId, String facultyEmail, BulkAttendanceRequest request);

    List<AttendanceResponse> getSectionAttendanceByDate(Long sectionId, String facultyEmail, LocalDate date);

    // Student endpoints
    List<AttendanceResponse> getMyAttendanceInSection(Long sectionId, String studentEmail);

    List<AttendanceSummaryResponse> getMyAttendanceSummary(String studentEmail);
}