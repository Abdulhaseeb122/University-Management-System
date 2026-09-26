package com.ums.service.impl;

import com.ums.dto.MembershipRequest;
import com.ums.dto.MembershipResponse;
import com.ums.entity.LibraryMembership;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.LibraryMembershipRepository;
import com.ums.repository.UserRepository;
import com.ums.service.MembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    private static final String ROLE_STUDENT = "ROLE_STUDENT";
    private static final String ROLE_FACULTY = "ROLE_FACULTY";

    private final LibraryMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public MembershipServiceImpl(LibraryMembershipRepository membershipRepository,
                                 UserRepository userRepository) {
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public MembershipResponse createMembership(MembershipRequest request) {
        // 1. User must exist in the university system
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found in university system: " + request.getUserId()));

        // 2. User must be ACTIVE (not deactivated)
        if (user.getIsActive() == null || !user.getIsActive()) {
            throw new BadRequestException("User account is inactive. Cannot register for library membership.");
        }

        // 3. ONLY Student or Faculty can become library members
        String roleName = user.getRole().getName();
        if (!ROLE_STUDENT.equals(roleName) && !ROLE_FACULTY.equals(roleName)) {
            throw new BadRequestException(
                    "Only students and faculty can have library memberships. User role: " + roleName);
        }

        // 4. User must not already have a membership (one per user)
        if (membershipRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("User already has a library membership.");
        }

        // 5. Card number must be globally unique
        if (membershipRepository.existsByLibraryCardNumber(request.getLibraryCardNumber())) {
            throw new BadRequestException(
                    "Library card number already in use: " + request.getLibraryCardNumber());
        }

        // 6. Build entity — membershipType AUTO-DERIVED from role
        LibraryMembership m = new LibraryMembership();
        m.setUser(user);
        m.setLibraryCardNumber(request.getLibraryCardNumber());
        m.setMembershipType(deriveMembershipType(roleName));  // <-- AUTO
        m.setContactPhone(request.getContactPhone());
        m.setAddress(request.getAddress());
        m.setValidUntil(request.getValidUntil());
        m.setStatus(LibraryMembership.MembershipStatus.ACTIVE);

        return mapToResponse(membershipRepository.save(m));
    }

    @Override
    public List<MembershipResponse> getAllMemberships() {
        return membershipRepository.findAll().stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public MembershipResponse getMembershipById(Long id) {
        LibraryMembership m = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));
        return mapToResponse(m);
    }

    @Override
    public MembershipResponse getMembershipByUserId(Long userId) {
        LibraryMembership m = membershipRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found for user: " + userId));
        return mapToResponse(m);
    }

    @Override
    @Transactional
    public MembershipResponse updateMembership(Long id, MembershipRequest request) {
        LibraryMembership m = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));

        // Card change → ensure uniqueness
        if (!m.getLibraryCardNumber().equals(request.getLibraryCardNumber())
                && membershipRepository.existsByLibraryCardNumber(request.getLibraryCardNumber())) {
            throw new BadRequestException("Card number already in use: " + request.getLibraryCardNumber());
        }

        // NOTE: user and membershipType are NOT updatable — they're locked.
        m.setLibraryCardNumber(request.getLibraryCardNumber());
        m.setContactPhone(request.getContactPhone());
        m.setAddress(request.getAddress());
        m.setValidUntil(request.getValidUntil());

        return mapToResponse(membershipRepository.save(m));
    }

    @Override
    @Transactional
    public void suspendMembership(Long id) {
        LibraryMembership m = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));
        m.setStatus(LibraryMembership.MembershipStatus.SUSPENDED);
        membershipRepository.save(m);
    }

    @Override
    @Transactional
    public void reactivateMembership(Long id) {
        LibraryMembership m = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));
        m.setStatus(LibraryMembership.MembershipStatus.ACTIVE);
        membershipRepository.save(m);
    }

    @Override
    @Transactional
    public void deleteMembership(Long id) {
        LibraryMembership m = membershipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membership not found: " + id));
        membershipRepository.delete(m);
    }

    @Override
    public MembershipResponse getMyMembership(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        LibraryMembership m = membershipRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "You don't have a library membership yet. Please contact library admin."));

        return mapToResponse(m);
    }

    // ---------------- Helpers ----------------

    private LibraryMembership.MembershipType deriveMembershipType(String roleName) {
        if (ROLE_STUDENT.equals(roleName)) return LibraryMembership.MembershipType.STUDENT;
        if (ROLE_FACULTY.equals(roleName)) return LibraryMembership.MembershipType.FACULTY;
        throw new BadRequestException("Cannot derive membership type from role: " + roleName);
    }

    private MembershipResponse mapToResponse(LibraryMembership m) {
        User u = m.getUser();
        boolean isValid = m.getStatus() == LibraryMembership.MembershipStatus.ACTIVE
                && !m.getValidUntil().isBefore(LocalDate.now());

        return MembershipResponse.builder()
                .id(m.getId())
                .userId(u.getId())
                .userFullName(u.getFirstName() + " " + u.getLastName())
                .userEmail(u.getEmail())
                .libraryCardNumber(m.getLibraryCardNumber())
                .membershipType(m.getMembershipType())
                .contactPhone(m.getContactPhone())
                .address(m.getAddress())
                .validUntil(m.getValidUntil())
                .status(m.getStatus())
                .registeredAt(m.getRegisteredAt())
                .valid(isValid)
                .build();
    }
}