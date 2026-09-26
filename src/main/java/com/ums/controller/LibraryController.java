package com.ums.controller;

import com.ums.dto.BookIssueResponse;
import com.ums.dto.BookResponse;
import com.ums.dto.MembershipResponse;
import com.ums.service.LibraryService;
import com.ums.service.MembershipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final MembershipService membershipService;

    public LibraryController(LibraryService libraryService,
                             MembershipService membershipService) {
        this.libraryService = libraryService;
        this.membershipService = membershipService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> availableBooks() {
        return ResponseEntity.ok(libraryService.getAvailableBooks());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookResponse> bookDetails(@PathVariable Long id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @GetMapping("/my-issues")
    public ResponseEntity<List<BookIssueResponse>> myIssues(Principal principal) {
        return ResponseEntity.ok(libraryService.getMyIssues(principal.getName()));
    }

    @GetMapping("/my-membership")
    public ResponseEntity<MembershipResponse> myMembership(Principal principal) {
        return ResponseEntity.ok(membershipService.getMyMembership(principal.getName()));
    }
}