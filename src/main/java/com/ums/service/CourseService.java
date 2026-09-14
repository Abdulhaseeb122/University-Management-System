package com.ums.service;

import com.ums.dto.CourseRequest;
import com.ums.dto.CourseResponse;

import java.util.List;

public interface CourseService {
    CourseResponse createCourse(CourseRequest request);
    List<CourseResponse> getAllCourses();
    CourseResponse getCourseById(Long id);
    CourseResponse updateCourse(Long id, CourseRequest request);
    void deleteCourse(Long id);
    List<CourseResponse> getCoursesByDepartment(Long departmentId);
}