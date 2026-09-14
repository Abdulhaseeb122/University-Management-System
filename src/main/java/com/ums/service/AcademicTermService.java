package com.ums.service;

import com.ums.dto.AcademicTermRequest;
import com.ums.dto.AcademicTermResponse;

import java.util.List;

public interface AcademicTermService {
    AcademicTermResponse createTerm(AcademicTermRequest request);
    List<AcademicTermResponse> getAllTerms();
    AcademicTermResponse getTermById(Long id);
    AcademicTermResponse updateTerm(Long id, AcademicTermRequest request);
    void deleteTerm(Long id);
    AcademicTermResponse setCurrentTerm(Long id);
}