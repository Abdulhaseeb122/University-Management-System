package com.ums.controller;

import com.ums.dto.MembershipRequest;
import com.ums.dto.MembershipResponse;
import com.ums.service.MembershipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/library/memberships")
public class AdminMembershipController {

    private final MembershipService membershipService;

    public AdminMembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> create(@Valid @RequestBody MembershipRequest request) {
        return new ResponseEntity<>(membershipService.createMembership(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getAll() {
        return ResponseEntity.ok(membershipService.getAllMemberships());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(membershipService.getMembershipById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<MembershipResponse> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipService.getMembershipByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MembershipResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody MembershipRequest request) {
        return ResponseEntity.ok(membershipService.updateMembership(id, request));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long id) {
        membershipService.suspendMembership(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        membershipService.reactivateMembership(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        membershipService.deleteMembership(id);
        return ResponseEntity.noContent().build();
    }
}