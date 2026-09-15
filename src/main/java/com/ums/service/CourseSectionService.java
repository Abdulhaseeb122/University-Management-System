package com.ums.service;

import com.ums.dto.CourseSectionRequest;
import com.ums.dto.CourseSectionResponse;

import java.util.List;

public interface CourseSectionService {
    CourseSectionResponse createSection(CourseSectionRequest request);
    List<CourseSectionResponse> getAllSections();
    CourseSectionResponse getSectionById(Long id);
    CourseSectionResponse updateSection(Long id, CourseSectionRequest request);
    void deleteSection(Long id);
    List<CourseSectionResponse> getSectionsByTerm(Long termId);
    List<CourseSectionResponse> getSectionsByCourse(Long courseId);
    List<CourseSectionResponse> getSectionsByFaculty(Long facultyId);
}