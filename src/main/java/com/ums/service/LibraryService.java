package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface LibraryService {

    // Book CRUD (Admin)
    BookResponse createBook(BookRequest request);
    List<BookResponse> getAllBooks();
    BookResponse getBookById(Long id);
    BookResponse updateBook(Long id, BookRequest request);
    void deleteBook(Long id);

    // Issue & Return (Admin)
    BookIssueResponse issueBook(IssueBookRequest request);
    BookIssueResponse returnBook(Long issueId);
    List<BookIssueResponse> getActiveIssues();
    List<BookIssueResponse> getOverdueIssues();

    // User
    List<BookResponse> getAvailableBooks();
    List<BookIssueResponse> getMyIssues(String userEmail);
}