package com.ums.service;

import com.ums.dto.FeeStructureRequest;
import com.ums.dto.FeeStructureResponse;

import java.util.List;

public interface FeeStructureService {
    FeeStructureResponse createFeeStructure(FeeStructureRequest request);
    List<FeeStructureResponse> getAllFeeStructures();
    FeeStructureResponse getFeeStructureById(Long id);
    FeeStructureResponse updateFeeStructure(Long id, FeeStructureRequest request);
    void deleteFeeStructure(Long id);
    List<FeeStructureResponse> getByTerm(Long termId);
    List<FeeStructureResponse> getByDepartment(Long departmentId);
}