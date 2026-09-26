package com.ums.service;

import com.ums.dto.MembershipRequest;
import com.ums.dto.MembershipResponse;

import java.util.List;

public interface MembershipService {

    MembershipResponse createMembership(MembershipRequest request);
    List<MembershipResponse> getAllMemberships();
    MembershipResponse getMembershipById(Long id);
    MembershipResponse getMembershipByUserId(Long userId);
    MembershipResponse updateMembership(Long id, MembershipRequest request);
    void suspendMembership(Long id);
    void reactivateMembership(Long id);
    void deleteMembership(Long id);

    MembershipResponse getMyMembership(String userEmail);
}