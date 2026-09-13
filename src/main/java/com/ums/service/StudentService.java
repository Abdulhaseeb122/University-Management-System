package com.ums.service;

import com.ums.dto.StudentProfileResponse;
import com.ums.dto.StudentUpdateRequest;

public interface StudentService {
    StudentProfileResponse getStudentProfile(String email);
    StudentProfileResponse updateStudentProfile(String email, StudentUpdateRequest request); // <-- NEW
}