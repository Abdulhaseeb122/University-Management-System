package com.ums.service.impl;

import com.ums.dto.CampusRequest;
import com.ums.dto.CampusResponse;
import com.ums.entity.Campus;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.CampusRepository;
import com.ums.service.CampusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CampusServiceImpl implements CampusService {

    private final CampusRepository campusRepository;

    public CampusServiceImpl(CampusRepository campusRepository) {
        this.campusRepository = campusRepository;
    }

    @Override
    @Transactional
    public CampusResponse createCampus(CampusRequest request) {
        if (campusRepository.existsByCode(request.getCode())) {
            throw new BadRequestException("Campus code already exists: " + request.getCode());
        }
        if (campusRepository.existsByName(request.getName())) {
            throw new BadRequestException("Campus name already exists: " + request.getName());
        }

        Campus campus = new Campus();
        campus.setName(request.getName());
        campus.setCode(request.getCode());
        campus.setAddress(request.getAddress());
        campus.setContactEmail(request.getContactEmail());

        Campus saved = campusRepository.save(campus);
        return mapToResponse(saved);
    }

    @Override
    public List<CampusResponse> getAllCampuses() {
        return campusRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CampusResponse getCampusById(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found with ID: " + id));
        return mapToResponse(campus);
    }

    private CampusResponse mapToResponse(Campus campus) {
        return CampusResponse.builder()
                .id(campus.getId())
                .name(campus.getName())
                .code(campus.getCode())
                .address(campus.getAddress())
                .contactEmail(campus.getContactEmail())
                .build();
    }
}