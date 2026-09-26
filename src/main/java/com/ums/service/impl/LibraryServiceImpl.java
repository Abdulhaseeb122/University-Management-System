package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.Book;
import com.ums.entity.BookIssue;
import com.ums.entity.LibraryMembership;
import com.ums.entity.User;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.BookIssueRepository;
import com.ums.repository.BookRepository;
import com.ums.repository.LibraryMembershipRepository;
import com.ums.repository.UserRepository;
import com.ums.service.LibraryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryServiceImpl implements LibraryService {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final int MAX_BOOKS_PER_USER = 3;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    private final BookRepository bookRepository;
    private final BookIssueRepository issueRepository;
    private final UserRepository userRepository;
    private final LibraryMembershipRepository membershipRepository;

    public LibraryServiceImpl(BookRepository bookRepository,
                              BookIssueRepository issueRepository,
                              UserRepository userRepository,
                              LibraryMembershipRepository membershipRepository) {
        this.bookRepository = bookRepository;
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    // ================================================================
    // ADMIN: Book CRUD
    // ================================================================
    @Override
    @Transactional
    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("A book with ISBN " + request.getIsbn() + " already exists.");
        }

        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getTotalCopies());

        return mapBookToResponse(bookRepository.save(book));
    }

    @Override
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapBookToResponse).collect(Collectors.toList());
    }

    @Override
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
        return mapBookToResponse(book);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BadRequestException("ISBN already in use: " + request.getIsbn());
        }

        int onLoan = book.getTotalCopies() - book.getAvailableCopies();
        if (request.getTotalCopies() < onLoan) {
            throw new BadRequestException(
                    "Cannot reduce total copies below " + onLoan + " (currently on loan).");
        }

        int newAvailable = request.getTotalCopies() - onLoan;

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(newAvailable);

        return mapBookToResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));

        int onLoan = book.getTotalCopies() - book.getAvailableCopies();
        if (onLoan > 0) {
            throw new BadRequestException(
                    "Cannot delete: " + onLoan + " copy(ies) currently on loan.");
        }

        bookRepository.delete(book);
    }

    // ================================================================
    // ADMIN: Issue Book (with MEMBERSHIP CHECK)
    // ================================================================
    @Override
    @Transactional
    public BookIssueResponse issueBook(IssueBookRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + request.getBookId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        // ---------- RULE 1: User must be a registered library member ----------
        LibraryMembership membership = membershipRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "User is not a library member. Please register them first."));

        // ---------- RULE 2: Membership must be ACTIVE ----------
        if (membership.getStatus() != LibraryMembership.MembershipStatus.ACTIVE) {
            throw new BadRequestException(
                    "Library membership is " + membership.getStatus() + ". Cannot issue books.");
        }

        // ---------- RULE 3: Membership must not be expired ----------
        if (membership.getValidUntil().isBefore(LocalDate.now())) {
            throw new BadRequestException("Library membership has expired. Please renew it.");
        }

        // ---------- RULE 4: User must have no overdue books ----------
        List<BookIssue> overdue = issueRepository.findByUserIdAndReturnDateIsNull(user.getId())
                .stream()
                .filter(i -> i.getDueDate().isBefore(LocalDate.now()))
                .toList();

        if (!overdue.isEmpty()) {
            throw new BadRequestException(
                    "User has " + overdue.size() + " overdue book(s). They must return them first.");
        }

        // ---------- RULE 5: Book must have available copies ----------
        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No copies available for: " + book.getTitle());
        }

        // ---------- RULE 6: Same book cannot be issued twice to same user ----------
        issueRepository.findByBookIdAndUserIdAndReturnDateIsNull(book.getId(), user.getId())
                .ifPresent(existing -> {
                    throw new BadRequestException("This user already has an unreturned copy of this book.");
                });

        // ---------- RULE 7: Max 3 books per user ----------
        long activeLoans = issueRepository.countByUserIdAndReturnDateIsNull(user.getId());
        if (activeLoans >= MAX_BOOKS_PER_USER) {
            throw new BadRequestException(
                    "User has reached the maximum limit of " + MAX_BOOKS_PER_USER + " books.");
        }

        // ---------- All checks passed — issue the book ----------
        LocalDate today = LocalDate.now();
        BookIssue issue = new BookIssue();
        issue.setBook(book);
        issue.setUser(user);
        issue.setIssueDate(today);
        issue.setDueDate(today.plusDays(LOAN_PERIOD_DAYS));
        issue.setFineAmount(BigDecimal.ZERO);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        return mapIssueToResponse(issueRepository.save(issue));
    }

    // ================================================================
    // ADMIN: Return Book
    // ================================================================
    @Override
    @Transactional
    public BookIssueResponse returnBook(Long issueId) {
        BookIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue record not found: " + issueId));

        if (issue.getReturnDate() != null) {
            throw new BadRequestException("This book has already been returned.");
        }

        LocalDate today = LocalDate.now();
        issue.setReturnDate(today);

        // Fine for late return
        if (today.isAfter(issue.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(issue.getDueDate(), today);
            BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate));
            issue.setFineAmount(fine);
        }

        Book book = issue.getBook();
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }

        return mapIssueToResponse(issueRepository.save(issue));
    }

    // ================================================================
    // ADMIN: Active / Overdue Issues
    // ================================================================
    @Override
    public List<BookIssueResponse> getActiveIssues() {
        return issueRepository.findByReturnDateIsNull().stream()
                .map(this::mapIssueToResponse).collect(Collectors.toList());
    }

    @Override
    public List<BookIssueResponse> getOverdueIssues() {
        return issueRepository.findByReturnDateIsNullAndDueDateBefore(LocalDate.now()).stream()
                .map(this::mapIssueToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // USER: Available Books
    // ================================================================
    @Override
    public List<BookResponse> getAvailableBooks() {
        return bookRepository.findByAvailableCopiesGreaterThan(0).stream()
                .map(this::mapBookToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // USER: My Issues
    // ================================================================
    @Override
    public List<BookIssueResponse> getMyIssues(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        return issueRepository.findByUserId(user.getId()).stream()
                .map(this::mapIssueToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // MAPPERS
    // ================================================================
    private BookResponse mapBookToResponse(Book b) {
        return BookResponse.builder()
                .id(b.getId())
                .isbn(b.getIsbn())
                .title(b.getTitle())
                .author(b.getAuthor())
                .publisher(b.getPublisher())
                .totalCopies(b.getTotalCopies())
                .availableCopies(b.getAvailableCopies())
                .build();
    }

    private BookIssueResponse mapIssueToResponse(BookIssue i) {
        boolean overdue = i.getReturnDate() == null
                && i.getDueDate() != null
                && LocalDate.now().isAfter(i.getDueDate());

        return BookIssueResponse.builder()
                .id(i.getId())
                .bookId(i.getBook().getId())
                .bookTitle(i.getBook().getTitle())
                .bookIsbn(i.getBook().getIsbn())
                .bookAuthor(i.getBook().getAuthor())
                .userId(i.getUser().getId())
                .userFullName(i.getUser().getFirstName() + " " + i.getUser().getLastName())
                .userEmail(i.getUser().getEmail())
                .issueDate(i.getIssueDate())
                .dueDate(i.getDueDate())
                .returnDate(i.getReturnDate())
                .fineAmount(i.getFineAmount())
                .overdue(overdue)
                .build();
    }
}