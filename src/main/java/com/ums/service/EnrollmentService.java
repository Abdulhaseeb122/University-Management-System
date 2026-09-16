package com.ums.service;

import com.ums.dto.CourseSectionResponse;
import com.ums.dto.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollStudent(String studentEmail, Long sectionId);

    EnrollmentResponse dropSection(String studentEmail, Long sectionId);

    List<EnrollmentResponse> getMyEnrollments(String studentEmail);

    List<CourseSectionResponse> getAvailableSections(String studentEmail);
}