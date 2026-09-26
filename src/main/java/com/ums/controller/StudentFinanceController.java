package com.ums.controller;

import com.ums.dto.InvoiceResponse;
import com.ums.dto.PaymentResponse;
import com.ums.dto.StudentFinanceSummaryResponse;
import com.ums.service.FinanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/student/finance")
public class StudentFinanceController {

    private final FinanceService financeService;

    public StudentFinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/my-invoices")
    public ResponseEntity<List<InvoiceResponse>> myInvoices(Principal principal) {
        return ResponseEntity.ok(financeService.getMyInvoices(principal.getName()));
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> myInvoice(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(financeService.getMyInvoice(id, principal.getName()));
    }

    @GetMapping("/invoices/{id}/payments")
    public ResponseEntity<List<PaymentResponse>> myPayments(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(financeService.getMyPayments(id, principal.getName()));
    }

    @GetMapping("/summary")
    public ResponseEntity<StudentFinanceSummaryResponse> mySummary(Principal principal) {
        return ResponseEntity.ok(financeService.getMySummary(principal.getName()));
    }
}