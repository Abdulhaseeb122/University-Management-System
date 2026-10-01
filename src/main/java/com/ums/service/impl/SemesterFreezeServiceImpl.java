package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.SemesterFreezeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemesterFreezeServiceImpl implements SemesterFreezeService {

    private final SemesterFreezeRepository freezeRepository;
    private final StudentRepository studentRepository;
    private final AcademicTermRepository termRepository;
    private final UserRepository userRepository;
    private final StudentInvoiceRepository invoiceRepository;
    private final BookIssueRepository bookIssueRepository;

    public SemesterFreezeServiceImpl(SemesterFreezeRepository freezeRepository,
                                     StudentRepository studentRepository,
                                     AcademicTermRepository termRepository,
                                     UserRepository userRepository,
                                     StudentInvoiceRepository invoiceRepository,
                                     BookIssueRepository bookIssueRepository) {
        this.freezeRepository = freezeRepository;
        this.studentRepository = studentRepository;
        this.termRepository = termRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.bookIssueRepository = bookIssueRepository;
    }

    // ================================================================
    // STUDENT: Apply for freeze
    // ================================================================
    @Override
    @Transactional
    public FreezeResponse applyForFreeze(String studentEmail, FreezeApplicationRequest request) {
        Student student = getStudent(studentEmail);

        // 1. Only ACTIVE students can apply
        if (student.getAcademicStatus() != Student.AcademicStatus.ACTIVE) {
            throw new BadRequestException(
                    "Only ACTIVE students can apply for a freeze. Current status: "
                            + student.getAcademicStatus());
        }

        // 2. No pending freeze request
        if (freezeRepository.existsByStudentIdAndStatus(student.getId(),
                SemesterFreeze.FreezeStatus.PENDING)) {
            throw new BadRequestException("You already have a pending freeze application.");
        }

        // 3. No active approved freeze
        List<SemesterFreeze.FreezeStatus> activeStatuses = List.of(
                SemesterFreeze.FreezeStatus.APPROVED,
                SemesterFreeze.FreezeStatus.RESUMED);
        // Only APPROVED is "active"; RESUMED means already back. Skip check if not APPROVED.
        if (freezeRepository.existsByStudentIdAndStatus(student.getId(),
                SemesterFreeze.FreezeStatus.APPROVED)) {
            throw new BadRequestException("You already have an active approved freeze.");
        }

        // 4. Validate terms
        AcademicTerm fromTerm = termRepository.findById(request.getFreezeFromTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found: " + request.getFreezeFromTermId()));
        AcademicTerm expectedReturnTerm = termRepository.findById(request.getExpectedReturnTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found: " + request.getExpectedReturnTermId()));

        // 5. Freeze-from term must be in future or current (not past)
        if (fromTerm.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Cannot apply for freeze in a past term.");
        }

        // 6. Expected return term must be after freeze-from term
        if (!expectedReturnTerm.getStartDate().isAfter(fromTerm.getEndDate())) {
            throw new BadRequestException(
                    "Expected return term must start after the freeze-from term ends.");
        }

        // 7. No pending dues: all invoices must be fully paid (or none exist)
        // Check only invoices for the freeze-from term
        // (Real universities check all outstanding dues — kept simple here)
        boolean hasUnpaidInvoices = invoiceRepository.findByStudentId(student.getId()).stream()
                .filter(inv -> inv.getTerm().getId().equals(fromTerm.getId()))
                .anyMatch(inv -> inv.getStatus() != StudentInvoice.InvoiceStatus.PAID);
        if (hasUnpaidInvoices) {
            throw new BadRequestException(
                    "You have unpaid invoices for the current term. Clear dues before applying.");
        }

        // 8. No unreturned library books
        long activeBookIssues = bookIssueRepository.findByUserIdAndReturnDateIsNull(student.getId()).size();
        if (activeBookIssues > 0) {
            throw new BadRequestException(
                    "You have " + activeBookIssues + " unreturned book(s). Return them before applying.");
        }

        // 9. Create freeze request
        SemesterFreeze freeze = new SemesterFreeze();
        freeze.setStudent(student);
        freeze.setFreezeFromTerm(fromTerm);
        freeze.setExpectedReturnTerm(expectedReturnTerm);
        freeze.setReason(request.getReason());
        freeze.setStatus(SemesterFreeze.FreezeStatus.PENDING);

        SemesterFreeze saved = freezeRepository.save(freeze);
        return mapToResponse(saved);
    }

    // ================================================================
    // STUDENT: History / Active
    // ================================================================
    @Override
    public List<FreezeResponse> getMyFreezeHistory(String studentEmail) {
        Student student = getStudent(studentEmail);
        return freezeRepository.findByStudentIdOrderByRequestedAtDesc(student.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public FreezeResponse getMyActiveFreeze(String studentEmail) {
        Student student = getStudent(studentEmail);

        SemesterFreeze freeze = freezeRepository
                .findFirstByStudentIdAndStatus(student.getId(),
                        SemesterFreeze.FreezeStatus.APPROVED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You don't have any approved freeze request."));

        return mapToResponse(freeze);
    }

    // ================================================================
    // STUDENT: Request resume
    // ================================================================
    @Override
    @Transactional
    public FreezeResponse requestResume(String studentEmail, ResumeRequest request) {
        Student student = getStudent(studentEmail);

        // Must have an active (approved) freeze
        SemesterFreeze activeFreeze = freezeRepository
                .findFirstByStudentIdAndStatus(student.getId(),
                        SemesterFreeze.FreezeStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException(
                        "You don't have an approved freeze to resume from."));

        // Validate return term
        AcademicTerm returnTerm = termRepository.findById(request.getReturnTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found: " + request.getReturnTermId()));

        // Return term must be in future or current
        if (returnTerm.getEndDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Return term cannot be in the past.");
        }

        // Update freeze request to PENDING resume (we use PENDING status again for resume approval)
        // To keep model simple: set status back to PENDING with updated return term
        activeFreeze.setActualReturnTerm(returnTerm);
        activeFreeze.setStatus(SemesterFreeze.FreezeStatus.PENDING);
        activeFreeze.setAdminRemarks(null);
        activeFreeze.setApprovedAt(null);
        activeFreeze.setApprovedBy(null);
        activeFreeze.setResumedAt(null);

        return mapToResponse(freezeRepository.save(activeFreeze));
    }

    // ================================================================
    // ADMIN: Get all / pending
    // ================================================================
    @Override
    public List<FreezeResponse> getAllFreezeRequests() {
        return freezeRepository.findAllByOrderByRequestedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<FreezeResponse> getPendingFreezeRequests() {
        return freezeRepository.findByStatusOrderByRequestedAtAsc(
                        SemesterFreeze.FreezeStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public FreezeResponse getFreezeById(Long id) {
        SemesterFreeze freeze = freezeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freeze not found: " + id));
        return mapToResponse(freeze);
    }

    // ================================================================
    // ADMIN: Approve freeze
    // ================================================================
    @Override
    @Transactional
    public FreezeResponse approveFreeze(Long id, String adminEmail, FreezeApprovalRequest request) {
        SemesterFreeze freeze = freezeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freeze not found: " + id));

        if (freeze.getStatus() != SemesterFreeze.FreezeStatus.PENDING) {
            throw new BadRequestException("Only PENDING freeze requests can be approved.");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        freeze.setStatus(SemesterFreeze.FreezeStatus.APPROVED);
        freeze.setApprovedAt(LocalDateTime.now());
        freeze.setApprovedBy(admin);
        freeze.setAdminRemarks(request.getRemarks());

        // Update student status → ON_LEAVE
        Student student = freeze.getStudent();
        student.setAcademicStatus(Student.AcademicStatus.ON_LEAVE);
        studentRepository.save(student);

        return mapToResponse(freezeRepository.save(freeze));
    }

    // ================================================================
    // ADMIN: Reject
    // ================================================================
    @Override
    @Transactional
    public FreezeResponse rejectFreeze(Long id, String adminEmail, FreezeApprovalRequest request) {
        SemesterFreeze freeze = freezeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freeze not found: " + id));

        if (freeze.getStatus() != SemesterFreeze.FreezeStatus.PENDING) {
            throw new BadRequestException("Only PENDING freeze requests can be rejected.");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        freeze.setStatus(SemesterFreeze.FreezeStatus.REJECTED);
        freeze.setApprovedAt(LocalDateTime.now());
        freeze.setApprovedBy(admin);
        freeze.setAdminRemarks(request.getRemarks());

        return mapToResponse(freezeRepository.save(freeze));
    }

    // ================================================================
    // ADMIN: Approve resume (PENDING → RESUMED)
    // ================================================================
    @Override
    @Transactional
    public FreezeResponse approveResume(Long id, String adminEmail, FreezeApprovalRequest request) {
        SemesterFreeze freeze = freezeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Freeze not found: " + id));

        // Must be a resumed request — we use PENDING with actualReturnTerm set
        if (freeze.getStatus() != SemesterFreeze.FreezeStatus.PENDING) {
            throw new BadRequestException("Only PENDING resume requests can be approved.");
        }
        if (freeze.getActualReturnTerm() == null) {
            throw new BadRequestException("This is a freeze request, not a resume request.");
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        freeze.setStatus(SemesterFreeze.FreezeStatus.RESUMED);
        freeze.setResumedAt(LocalDateTime.now());
        freeze.setApprovedBy(admin);
        freeze.setAdminRemarks(request.getRemarks());

        // Update student status → ACTIVE
        Student student = freeze.getStudent();
        student.setAcademicStatus(Student.AcademicStatus.ACTIVE);
        studentRepository.save(student);

        return mapToResponse(freezeRepository.save(freeze));
    }

    @Override
    public List<FreezeResponse> getStudentFreezeHistory(Long studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found: " + studentId);
        }
        return freezeRepository.findByStudentIdOrderByRequestedAtDesc(studentId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Student getStudent(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + email));
    }

    private FreezeResponse mapToResponse(SemesterFreeze f) {
        Student s = f.getStudent();

        return FreezeResponse.builder()
                .id(f.getId())
                .studentId(s.getId())
                .studentRollNumber(s.getRollNumber())
                .studentFullName(s.getUser().getFirstName() + " " + s.getUser().getLastName())
                .freezeFromTermId(f.getFreezeFromTerm().getId())
                .freezeFromTermName(f.getFreezeFromTerm().getName())
                .expectedReturnTermId(f.getExpectedReturnTerm() != null
                        ? f.getExpectedReturnTerm().getId() : null)
                .expectedReturnTermName(f.getExpectedReturnTerm() != null
                        ? f.getExpectedReturnTerm().getName() : null)
                .actualReturnTermId(f.getActualReturnTerm() != null
                        ? f.getActualReturnTerm().getId() : null)
                .actualReturnTermName(f.getActualReturnTerm() != null
                        ? f.getActualReturnTerm().getName() : null)
                .reason(f.getReason())
                .status(f.getStatus())
                .requestedAt(f.getRequestedAt())
                .approvedAt(f.getApprovedAt())
                .resumedAt(f.getResumedAt())
                .approvedById(f.getApprovedBy() != null ? f.getApprovedBy().getId() : null)
                .approvedByName(f.getApprovedBy() != null
                        ? f.getApprovedBy().getFirstName() + " " + f.getApprovedBy().getLastName()
                        : null)
                .adminRemarks(f.getAdminRemarks())
                .build();
    }
}