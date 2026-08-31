package com.ums.service;

import com.ums.dto.LoginRequest;
import com.ums.dto.RegisterRequest;
import com.ums.dto.StudentResponse;

public interface AuthService {
    StudentResponse registerUser(RegisterRequest request);
    String login(LoginRequest request);
}