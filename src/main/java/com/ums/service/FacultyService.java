package com.ums.service;

import com.ums.dto.FacultyRequest;
import com.ums.dto.FacultyResponse;

import java.util.List;

public interface FacultyService {
    FacultyResponse createFaculty(FacultyRequest request);
    List<FacultyResponse> getAllFaculty();
    FacultyResponse getFacultyById(Long id);
    FacultyResponse updateFaculty(Long id, FacultyRequest request);
    void deleteFaculty(Long id);
    List<FacultyResponse> getFacultyByDepartment(Long departmentId);
}