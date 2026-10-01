package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface SemesterFreezeService {

    // Student
    FreezeResponse applyForFreeze(String studentEmail, FreezeApplicationRequest request);
    List<FreezeResponse> getMyFreezeHistory(String studentEmail);
    FreezeResponse getMyActiveFreeze(String studentEmail);
    FreezeResponse requestResume(String studentEmail, ResumeRequest request);

    // Admin
    List<FreezeResponse> getAllFreezeRequests();
    List<FreezeResponse> getPendingFreezeRequests();
    FreezeResponse getFreezeById(Long id);
    FreezeResponse approveFreeze(Long id, String adminEmail, FreezeApprovalRequest request);
    FreezeResponse rejectFreeze(Long id, String adminEmail, FreezeApprovalRequest request);
    FreezeResponse approveResume(Long id, String adminEmail, FreezeApprovalRequest request);
    List<FreezeResponse> getStudentFreezeHistory(Long studentId);
}