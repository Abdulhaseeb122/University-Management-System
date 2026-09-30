package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface CourseEvaluationService {

    // Student
    CourseEvaluationResponse submitEvaluation(String studentEmail, Long sectionId, CourseEvaluationRequest request);
    List<CourseEvaluationResponse> getMyEvaluations(String studentEmail);
    CourseEvaluationResponse getMyEvaluationForSection(String studentEmail, Long sectionId);
    List<CourseSectionResponse> getMyPendingEvaluations(String studentEmail);

    // Admin
    List<AdminEvaluationResponse> getAllEvaluations();
    List<AdminEvaluationResponse> getEvaluationsBySection(Long sectionId);
    List<AdminEvaluationResponse> getEvaluationsByFaculty(Long facultyId);
    List<AdminEvaluationResponse> getEvaluationsByCourse(Long courseId);
    SectionEvaluationSummary getSectionSummary(Long sectionId);
    FacultyEvaluationSummary getFacultySummary(Long facultyId);
}