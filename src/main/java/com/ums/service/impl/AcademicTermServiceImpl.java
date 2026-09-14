package com.ums.service.impl;

import com.ums.dto.AcademicTermRequest;
import com.ums.dto.AcademicTermResponse;
import com.ums.entity.AcademicTerm;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.AcademicTermRepository;
import com.ums.service.AcademicTermService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AcademicTermServiceImpl implements AcademicTermService {

    private final AcademicTermRepository termRepository;

    public AcademicTermServiceImpl(AcademicTermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Override
    @Transactional
    public AcademicTermResponse createTerm(AcademicTermRequest request) {
        // 1. Validate start date is before end date
        validateDates(request);

        // 2. Uniqueness check on term code
        if (termRepository.existsByTermCode(request.getTermCode())) {
            throw new BadRequestException("Term code already exists: " + request.getTermCode());
        }

        AcademicTerm term = new AcademicTerm();
        term.setName(request.getName());
        term.setTermCode(request.getTermCode());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());

        // 3. Handle isCurrent flag
        boolean shouldBeCurrent = Boolean.TRUE.equals(request.getIsCurrent());
        if (shouldBeCurrent) {
            clearCurrentTerm(); // Un-flag the previous current term
        }
        term.setIsCurrent(shouldBeCurrent);

        AcademicTerm saved = termRepository.save(term);
        return mapToResponse(saved);
    }

    @Override
    public List<AcademicTermResponse> getAllTerms() {
        return termRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AcademicTermResponse getTermById(Long id) {
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with ID: " + id));
        return mapToResponse(term);
    }

    @Override
    @Transactional
    public AcademicTermResponse updateTerm(Long id, AcademicTermRequest request) {
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with ID: " + id));

        validateDates(request);

        // If term code is being changed, check for uniqueness
        if (!term.getTermCode().equals(request.getTermCode())
                && termRepository.existsByTermCode(request.getTermCode())) {
            throw new BadRequestException("Term code already exists: " + request.getTermCode());
        }

        term.setName(request.getName());
        term.setTermCode(request.getTermCode());
        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());

        // Handle isCurrent flag change
        if (Boolean.TRUE.equals(request.getIsCurrent()) && !Boolean.TRUE.equals(term.getIsCurrent())) {
            clearCurrentTerm();
            term.setIsCurrent(true);
        } else if (Boolean.FALSE.equals(request.getIsCurrent())) {
            term.setIsCurrent(false);
        }

        AcademicTerm updated = termRepository.save(term);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTerm(Long id) {
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with ID: " + id));

        // Safety: prevent deleting the current active term
        if (Boolean.TRUE.equals(term.getIsCurrent())) {
            throw new BadRequestException("Cannot delete the currently active term. Please set another term as current first.");
        }

        termRepository.delete(term);
    }

    @Override
    @Transactional
    public AcademicTermResponse setCurrentTerm(Long id) {
        AcademicTerm term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic term not found with ID: " + id));

        // Clear the previous current term
        clearCurrentTerm();

        // Set new current term
        term.setIsCurrent(true);
        AcademicTerm saved = termRepository.save(term);
        return mapToResponse(saved);
    }

    // ---------------- Helper Methods ----------------

    private void validateDates(AcademicTermRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())
                || request.getStartDate().isEqual(request.getEndDate())) {
            throw new BadRequestException("Start date must be strictly before end date.");
        }
    }

    private void clearCurrentTerm() {
        Optional<AcademicTerm> existingCurrent = termRepository.findByIsCurrentTrue();
        existingCurrent.ifPresent(t -> {
            t.setIsCurrent(false);
            termRepository.save(t);
        });
    }

    private AcademicTermResponse mapToResponse(AcademicTerm t) {
        return AcademicTermResponse.builder()
                .id(t.getId())
                .name(t.getName())
                .termCode(t.getTermCode())
                .startDate(t.getStartDate())
                .endDate(t.getEndDate())
                .isCurrent(t.getIsCurrent())
                .build();
    }
}