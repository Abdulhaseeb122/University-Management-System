package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface ExamService {

    // Admin - Exams
    ExamResponse createExam(ExamRequest request);
    List<ExamResponse> getAllExams();
    ExamResponse getExamById(Long id);
    ExamResponse updateExam(Long id, ExamRequest request);
    void deleteExam(Long id);
    List<ExamResponse> getExamsByTerm(Long termId);
    List<ExamResponse> getExamsByCourse(Long courseId);

    // Admin - Seating
    List<ExamSeatingResponse> allocateSeats(Long examId);
    List<ExamSeatingResponse> getExamSeating(Long examId);
    void clearSeating(Long examId);
    List<ExamSeatingResponse> assignInvigilator(Long examId, Long facultyId);

    // Student
    List<ExamResponse> getMyUpcomingExams(String studentEmail);
    List<ExamSeatingResponse> getMySeating(String studentEmail);
    ExamSeatingResponse getMySeatForExam(String studentEmail, Long examId);

    // Faculty
    List<ExamSeatingResponse> getMyInvigilations(String facultyEmail);
    List<ExamSeatingResponse> getStudentsForInvigilatedExam(String facultyEmail, Long examId);
}