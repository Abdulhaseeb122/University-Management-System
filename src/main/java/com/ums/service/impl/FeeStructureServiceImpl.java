package com.ums.service.impl;

import com.ums.dto.FeeStructureRequest;
import com.ums.dto.FeeStructureResponse;
import com.ums.entity.AcademicTerm;
import com.ums.entity.Department;
import com.ums.entity.FeeStructure;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.AcademicTermRepository;
import com.ums.repository.DepartmentRepository;
import com.ums.repository.FeeStructureRepository;
import com.ums.service.FeeStructureService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeeStructureServiceImpl implements FeeStructureService {

    private final FeeStructureRepository feeStructureRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicTermRepository termRepository;

    public FeeStructureServiceImpl(FeeStructureRepository feeStructureRepository,
                                   DepartmentRepository departmentRepository,
                                   AcademicTermRepository termRepository) {
        this.feeStructureRepository = feeStructureRepository;
        this.departmentRepository = departmentRepository;
        this.termRepository = termRepository;
    }

    @Override
    @Transactional
    public FeeStructureResponse createFeeStructure(FeeStructureRequest request) {
        // 1. Validate department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + request.getDepartmentId()));

        // 2. Validate term
        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic term not found with ID: " + request.getTermId()));

        // 3. Uniqueness: one fee structure per department per term
        if (feeStructureRepository.existsByDepartmentIdAndTermId(
                request.getDepartmentId(), request.getTermId())) {
            throw new BadRequestException(
                    "Fee structure already exists for " + department.getName()
                            + " in " + term.getName());
        }

        FeeStructure fs = new FeeStructure();
        fs.setDepartment(department);
        fs.setTerm(term);
        fs.setTuitionFeePerCredit(request.getTuitionFeePerCredit());
        fs.setLibraryFee(request.getLibraryFee() != null ? request.getLibraryFee() : BigDecimal.ZERO);
        fs.setLabFee(request.getLabFee() != null ? request.getLabFee() : BigDecimal.ZERO);
        fs.setHostelFee(request.getHostelFee() != null ? request.getHostelFee() : BigDecimal.ZERO);

        return mapToResponse(feeStructureRepository.save(fs));
    }

    @Override
    public List<FeeStructureResponse> getAllFeeStructures() {
        return feeStructureRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public FeeStructureResponse getFeeStructureById(Long id) {
        FeeStructure fs = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found: " + id));
        return mapToResponse(fs);
    }

    @Override
    @Transactional
    public FeeStructureResponse updateFeeStructure(Long id, FeeStructureRequest request) {
        FeeStructure fs = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found: " + id));

        // If dept or term is changing, ensure new combination is unique
        boolean deptChanged = !fs.getDepartment().getId().equals(request.getDepartmentId());
        boolean termChanged = !fs.getTerm().getId().equals(request.getTermId());

        if (deptChanged || termChanged) {
            if (feeStructureRepository.existsByDepartmentIdAndTermId(
                    request.getDepartmentId(), request.getTermId())) {
                throw new BadRequestException("Fee structure already exists for this department and term.");
            }

            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found: " + request.getDepartmentId()));
            AcademicTerm term = termRepository.findById(request.getTermId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Term not found: " + request.getTermId()));

            fs.setDepartment(department);
            fs.setTerm(term);
        }

        fs.setTuitionFeePerCredit(request.getTuitionFeePerCredit());
        fs.setLibraryFee(request.getLibraryFee() != null ? request.getLibraryFee() : BigDecimal.ZERO);
        fs.setLabFee(request.getLabFee() != null ? request.getLabFee() : BigDecimal.ZERO);
        fs.setHostelFee(request.getHostelFee() != null ? request.getHostelFee() : BigDecimal.ZERO);

        return mapToResponse(feeStructureRepository.save(fs));
    }

    @Override
    @Transactional
    public void deleteFeeStructure(Long id) {
        FeeStructure fs = feeStructureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fee structure not found: " + id));
        feeStructureRepository.delete(fs);
    }

    @Override
    public List<FeeStructureResponse> getByTerm(Long termId) {
        if (!termRepository.existsById(termId)) {
            throw new ResourceNotFoundException("Term not found: " + termId);
        }
        return feeStructureRepository.findByTermId(termId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<FeeStructureResponse> getByDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found: " + departmentId);
        }
        return feeStructureRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    private FeeStructureResponse mapToResponse(FeeStructure fs) {
        return FeeStructureResponse.builder()
                .id(fs.getId())
                .departmentId(fs.getDepartment().getId())
                .departmentName(fs.getDepartment().getName())
                .departmentCode(fs.getDepartment().getCode())
                .termId(fs.getTerm().getId())
                .termName(fs.getTerm().getName())
                .termCode(fs.getTerm().getTermCode())
                .tuitionFeePerCredit(fs.getTuitionFeePerCredit())
                .libraryFee(fs.getLibraryFee())
                .labFee(fs.getLabFee())
                .hostelFee(fs.getHostelFee())
                .build();
    }
}