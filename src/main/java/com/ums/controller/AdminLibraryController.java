package com.ums.controller;

import com.ums.dto.*;
import com.ums.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/library")
public class AdminLibraryController {

    private final LibraryService libraryService;

    public AdminLibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // ---------- Books ----------

    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return new ResponseEntity<>(libraryService.createBook(request), HttpStatus.CREATED);
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(libraryService.getAllBooks());
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        return ResponseEntity.ok(libraryService.getBookById(id));
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<BookResponse> updateBook(@PathVariable Long id,
                                                   @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(libraryService.updateBook(id, request));
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        libraryService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Issues ----------

    @PostMapping("/issues")
    public ResponseEntity<BookIssueResponse> issueBook(@Valid @RequestBody IssueBookRequest request) {
        return new ResponseEntity<>(libraryService.issueBook(request), HttpStatus.CREATED);
    }

    @PostMapping("/issues/{issueId}/return")
    public ResponseEntity<BookIssueResponse> returnBook(@PathVariable Long issueId) {
        return ResponseEntity.ok(libraryService.returnBook(issueId));
    }

    @GetMapping("/issues")
    public ResponseEntity<List<BookIssueResponse>> activeIssues() {
        return ResponseEntity.ok(libraryService.getActiveIssues());
    }

    @GetMapping("/issues/overdue")
    public ResponseEntity<List<BookIssueResponse>> overdueIssues() {
        return ResponseEntity.ok(libraryService.getOverdueIssues());
    }
}