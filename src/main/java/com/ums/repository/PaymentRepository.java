package com.ums.repository;

import com.ums.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    Optional<Payment> findByTransactionReference(String transactionReference);

    boolean existsByTransactionReference(String transactionReference);

    // Sum of successful payments for an invoice
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p " +
            "WHERE p.invoice.id = :invoiceId AND p.status = 'SUCCESS'")
    BigDecimal sumSuccessfulPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);
}