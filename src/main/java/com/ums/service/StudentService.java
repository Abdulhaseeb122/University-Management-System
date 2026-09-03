package com.ums.service;

import com.ums.dto.StudentProfileResponse;

public interface StudentService {
    StudentProfileResponse getStudentProfile(String email);
}