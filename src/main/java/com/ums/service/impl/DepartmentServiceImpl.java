package com.ums.service.impl;

import com.ums.dto.DepartmentRequest;
import com.ums.dto.DepartmentResponse;
import com.ums.entity.Campus;
import com.ums.entity.Department;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.CampusRepository;
import com.ums.repository.DepartmentRepository;
import com.ums.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final CampusRepository campusRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 CampusRepository campusRepository) {
        this.departmentRepository = departmentRepository;
        this.campusRepository = campusRepository;
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Campus campus = campusRepository.findById(request.getCampusId())
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found with ID: " + request.getCampusId()));

        if (departmentRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Department code already exists: " + request.getCode());
        }

        Department department = new Department();
        department.setCampus(campus);
        department.setName(request.getName());
        department.setCode(request.getCode());

        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
        return mapToResponse(department);
    }

    private DepartmentResponse mapToResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .campusId(d.getCampus().getId())
                .campusName(d.getCampus().getName())
                .build();
    }
}