package com.ums.service.impl;

import com.ums.dto.*;
import com.ums.entity.*;
import com.ums.exception.BadRequestException;
import com.ums.exception.ResourceNotFoundException;
import com.ums.repository.*;
import com.ums.service.FinanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final StudentInvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicTermRepository termRepository;

    public FinanceServiceImpl(StudentInvoiceRepository invoiceRepository,
                              PaymentRepository paymentRepository,
                              FeeStructureRepository feeStructureRepository,
                              EnrollmentRepository enrollmentRepository,
                              StudentRepository studentRepository,
                              AcademicTermRepository termRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.feeStructureRepository = feeStructureRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.termRepository = termRepository;
    }

    // ================================================================
    // ADMIN: Generate Invoices for All Enrolled Students in a Term
    // ================================================================
    @Override
    @Transactional
    public List<InvoiceResponse> generateInvoices(GenerateInvoicesRequest request) {
        AcademicTerm term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Term not found: " + request.getTermId()));

        if (request.getDueDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Due date cannot be in the past.");
        }

        // Get all active enrollments for this term
        List<Enrollment> enrollments = enrollmentRepository.findAll().stream()
                .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ENROLLED)
                .filter(e -> e.getSection().getTerm().getId().equals(term.getId()))
                .collect(Collectors.toList());

        if (enrollments.isEmpty()) {
            throw new BadRequestException("No active enrollments found for term: " + term.getName());
        }

        // Group enrollments by student
        List<InvoiceResponse> generated = new ArrayList<>();

        for (Enrollment e : enrollments) {
            Student student = e.getStudent();

            // Skip if invoice already exists for this student+term
            if (invoiceRepository.existsByStudentIdAndTermId(student.getId(), term.getId())) {
                continue;
            }

            // Fetch fee structure for the student's department
            FeeStructure fs = feeStructureRepository
                    .findByDepartmentIdAndTermId(student.getDepartment().getId(), term.getId())
                    .orElseThrow(() -> new BadRequestException(
                            "No fee structure found for department " + student.getDepartment().getCode()
                                    + " in term " + term.getName()));

            // Sum enrolled credits for this student in this term
            int totalCredits = enrollments.stream()
                    .filter(x -> x.getStudent().getId().equals(student.getId()))
                    .mapToInt(x -> x.getSection().getCourse().getCredits())
                    .sum();

            // Compute total amount
            BigDecimal tuition = fs.getTuitionFeePerCredit().multiply(BigDecimal.valueOf(totalCredits));
            BigDecimal total = tuition
                    .add(fs.getLibraryFee())
                    .add(fs.getLabFee())
                    .add(fs.getHostelFee());

            // Create invoice
            StudentInvoice invoice = new StudentInvoice();
            invoice.setStudent(student);
            invoice.setTerm(term);
            invoice.setInvoiceNumber(generateInvoiceNumber(term));
            invoice.setTotalAmount(total);
            invoice.setDueDate(request.getDueDate());
            invoice.setStatus(StudentInvoice.InvoiceStatus.UNPAID);

            invoiceRepository.save(invoice);
            generated.add(mapInvoiceToResponse(invoice));
        }

        return generated;
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapInvoiceToResponse).collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse getInvoiceById(Long id) {
        StudentInvoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        return mapInvoiceToResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getInvoicesByTerm(Long termId) {
        if (!termRepository.existsById(termId)) {
            throw new ResourceNotFoundException("Term not found: " + termId);
        }
        return invoiceRepository.findByTermId(termId).stream()
                .map(this::mapInvoiceToResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceResponse> getInvoicesByStatus(String status) {
        try {
            StudentInvoice.InvoiceStatus s = StudentInvoice.InvoiceStatus.valueOf(status.toUpperCase());
            return invoiceRepository.findByStatus(s).stream()
                    .map(this::mapInvoiceToResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }
    }

    @Override
    public List<InvoiceResponse> getOverdueInvoices() {
        // Also update status dynamically to OVERDUE if past due
        List<StudentInvoice.InvoiceStatus> unpaidStatuses = List.of(
                StudentInvoice.InvoiceStatus.UNPAID,
                StudentInvoice.InvoiceStatus.PARTIALLY_PAID);

        List<StudentInvoice> overdue = invoiceRepository
                .findByStatusInAndDueDateBefore(unpaidStatuses, LocalDate.now());

        // Mark them as OVERDUE if not already
        for (StudentInvoice inv : overdue) {
            if (inv.getStatus() != StudentInvoice.InvoiceStatus.OVERDUE) {
                inv.setStatus(StudentInvoice.InvoiceStatus.OVERDUE);
                invoiceRepository.save(inv);
            }
        }

        return overdue.stream().map(this::mapInvoiceToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // ADMIN: Record Payment
    // ================================================================
    @Override
    @Transactional
    public PaymentResponse recordPayment(Long invoiceId, PaymentRequest request) {
        StudentInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        // 1. Transaction reference must be unique
        if (paymentRepository.existsByTransactionReference(request.getTransactionReference())) {
            throw new BadRequestException(
                    "Transaction reference already used: " + request.getTransactionReference());
        }

        // 2. Calculate remaining balance
        BigDecimal alreadyPaid = paymentRepository.sumSuccessfulPaymentsByInvoiceId(invoiceId);
        BigDecimal remaining = invoice.getTotalAmount().subtract(alreadyPaid);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invoice is already fully paid.");
        }

        // 3. Cannot overpay
        if (request.getAmountPaid().compareTo(remaining) > 0) {
            throw new BadRequestException(
                    "Payment amount (" + request.getAmountPaid() + ") exceeds remaining balance (" + remaining + ")");
        }

        // 4. Create payment
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setTransactionReference(request.getTransactionReference());
        payment.setAmountPaid(request.getAmountPaid());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);

        Payment saved = paymentRepository.save(payment);

        // 5. Update invoice status
        BigDecimal newTotalPaid = alreadyPaid.add(request.getAmountPaid());
        if (newTotalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus(StudentInvoice.InvoiceStatus.PAID);
        } else {
            invoice.setStatus(StudentInvoice.InvoiceStatus.PARTIALLY_PAID);
        }

        // Check if it should be marked overdue
        if (invoice.getDueDate().isBefore(LocalDate.now())
                && invoice.getStatus() != StudentInvoice.InvoiceStatus.PAID) {
            invoice.setStatus(StudentInvoice.InvoiceStatus.OVERDUE);
        }

        invoiceRepository.save(invoice);

        return mapPaymentToResponse(saved);
    }

    @Override
    public List<PaymentResponse> getPaymentsForInvoice(Long invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found: " + invoiceId);
        }
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapPaymentToResponse).collect(Collectors.toList());
    }

    // ================================================================
    // STUDENT: My Invoices
    // ================================================================
    @Override
    public List<InvoiceResponse> getMyInvoices(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        return invoiceRepository.findByStudentId(student.getId()).stream()
                .map(this::mapInvoiceToResponse).collect(Collectors.toList());
    }

    @Override
    public InvoiceResponse getMyInvoice(Long invoiceId, String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        StudentInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        // IDOR protection
        if (!invoice.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("You cannot view another student's invoice.");
        }
        return mapInvoiceToResponse(invoice);
    }

    @Override
    public List<PaymentResponse> getMyPayments(Long invoiceId, String studentEmail) {
        Student student = getStudentByEmail(studentEmail);
        StudentInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (!invoice.getStudent().getId().equals(student.getId())) {
            throw new BadRequestException("You cannot view another student's payments.");
        }

        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(this::mapPaymentToResponse).collect(Collectors.toList());
    }

    @Override
    public StudentFinanceSummaryResponse getMySummary(String studentEmail) {
        Student student = getStudentByEmail(studentEmail);

        List<StudentInvoice> invoices = invoiceRepository.findByStudentId(student.getId());

        BigDecimal totalBilled = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        long overdueCount = 0;
        long paidCount = 0;

        for (StudentInvoice inv : invoices) {
            totalBilled = totalBilled.add(inv.getTotalAmount());
            BigDecimal paid = paymentRepository.sumSuccessfulPaymentsByInvoiceId(inv.getId());
            totalPaid = totalPaid.add(paid);

            if (inv.getStatus() == StudentInvoice.InvoiceStatus.PAID) paidCount++;
            if (inv.getStatus() == StudentInvoice.InvoiceStatus.OVERDUE) overdueCount++;
        }

        return StudentFinanceSummaryResponse.builder()
                .studentId(student.getId())
                .studentRollNumber(student.getRollNumber())
                .studentFullName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .totalInvoices(invoices.size())
                .totalBilled(totalBilled)
                .totalPaid(totalPaid)
                .totalOutstanding(totalBilled.subtract(totalPaid))
                .overdueCount(overdueCount)
                .paidCount(paidCount)
                .build();
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Student getStudentByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student profile not found for email: " + email));
    }

    private String generateInvoiceNumber(AcademicTerm term) {
        // Format: INV-{TERMCODE}-{6-char-random}
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "INV-" + term.getTermCode() + "-" + suffix;
    }

    private InvoiceResponse mapInvoiceToResponse(StudentInvoice inv) {
        BigDecimal paid = paymentRepository.sumSuccessfulPaymentsByInvoiceId(inv.getId());
        BigDecimal remaining = inv.getTotalAmount().subtract(paid);

        Student student = inv.getStudent();
        return InvoiceResponse.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .totalAmount(inv.getTotalAmount())
                .paidAmount(paid)
                .remainingAmount(remaining)
                .dueDate(inv.getDueDate())
                .status(inv.getStatus())
                .createdAt(inv.getCreatedAt())
                .studentId(student.getId())
                .studentRollNumber(student.getRollNumber())
                .studentFullName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .studentEmail(student.getUser().getEmail())
                .termId(inv.getTerm().getId())
                .termName(inv.getTerm().getName())
                .termCode(inv.getTerm().getTermCode())
                .build();
    }

    private PaymentResponse mapPaymentToResponse(Payment p) {
        StudentInvoice inv = p.getInvoice();
        Student student = inv.getStudent();

        return PaymentResponse.builder()
                .id(p.getId())
                .invoiceId(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .transactionReference(p.getTransactionReference())
                .amountPaid(p.getAmountPaid())
                .paymentMethod(p.getPaymentMethod())
                .status(p.getStatus())
                .paymentDate(p.getPaymentDate())
                .studentId(student.getId())
                .studentFullName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .build();
    }
}