package com.ums.service;

import com.ums.dto.*;

import java.util.List;

public interface FinanceService {

    // ---- Invoices ----
    List<InvoiceResponse> generateInvoices(GenerateInvoicesRequest request);
    List<InvoiceResponse> getAllInvoices();
    InvoiceResponse getInvoiceById(Long id);
    List<InvoiceResponse> getInvoicesByTerm(Long termId);
    List<InvoiceResponse> getInvoicesByStatus(String status);
    List<InvoiceResponse> getOverdueInvoices();

    // ---- Payments ----
    PaymentResponse recordPayment(Long invoiceId, PaymentRequest request);
    List<PaymentResponse> getPaymentsForInvoice(Long invoiceId);

    // ---- Student ----
    List<InvoiceResponse> getMyInvoices(String studentEmail);
    InvoiceResponse getMyInvoice(Long invoiceId, String studentEmail);
    List<PaymentResponse> getMyPayments(Long invoiceId, String studentEmail);
    StudentFinanceSummaryResponse getMySummary(String studentEmail);
}