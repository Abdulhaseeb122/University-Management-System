package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface GradeService {

    // ---- Faculty: Grade Items ----
    GradeItemResponse createGradeItem(Long sectionId, String facultyEmail, GradeItemRequest request);
    List<GradeItemResponse> getGradeItems(Long sectionId, String facultyEmail);
    void deleteGradeItem(Long gradeItemId, String facultyEmail);

    // ---- Faculty: Marks Entry ----
    List<StudentGradeResponse> enterBulkMarks(Long gradeItemId, String facultyEmail, BulkMarksRequest request);
    List<StudentGradeResponse> getMarksForGradeItem(Long gradeItemId, String facultyEmail);

    // ---- Faculty: Final Grades ----
    List<FinalCourseGradeResponse> computeFinalGrades(Long sectionId, String facultyEmail);
    List<FinalCourseGradeResponse> publishFinalGrades(Long sectionId, String facultyEmail);

    // ---- Student ----
    List<FinalCourseGradeResponse> getMyGradesForSection(Long sectionId, String studentEmail);
    List<FinalCourseGradeResponse> getMyTranscript(String studentEmail);
}