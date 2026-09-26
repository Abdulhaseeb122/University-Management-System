package com.ums.repository;

import com.ums.entity.StudentInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentInvoiceRepository extends JpaRepository<StudentInvoice, Long> {

    Optional<StudentInvoice> findByInvoiceNumber(String invoiceNumber);

    List<StudentInvoice> findByStudentId(Long studentId);

    List<StudentInvoice> findByTermId(Long termId);

    List<StudentInvoice> findByStatus(StudentInvoice.InvoiceStatus status);

    // Prevent duplicate invoice for same student + term
    boolean existsByStudentIdAndTermId(Long studentId, Long termId);

    Optional<StudentInvoice> findByStudentIdAndTermId(Long studentId, Long termId);

    // Overdue detection
    List<StudentInvoice> findByStatusInAndDueDateBefore(
            List<StudentInvoice.InvoiceStatus> statuses, LocalDate date);
}