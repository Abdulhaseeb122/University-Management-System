package com.ums.controller;

import com.ums.dto.*;
import com.ums.service.FeeStructureService;
import com.ums.service.FinanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/finance")
public class AdminFinanceController {

    private final FeeStructureService feeStructureService;
    private final FinanceService financeService;

    public AdminFinanceController(FeeStructureService feeStructureService,
                                  FinanceService financeService) {
        this.feeStructureService = feeStructureService;
        this.financeService = financeService;
    }

    // ---------- Fee Structures ----------

    @PostMapping("/fee-structures")
    public ResponseEntity<FeeStructureResponse> createFeeStructure(
            @Valid @RequestBody FeeStructureRequest request) {
        return new ResponseEntity<>(feeStructureService.createFeeStructure(request), HttpStatus.CREATED);
    }

    @GetMapping("/fee-structures")
    public ResponseEntity<List<FeeStructureResponse>> getAllFeeStructures() {
        return ResponseEntity.ok(feeStructureService.getAllFeeStructures());
    }

    @GetMapping("/fee-structures/{id}")
    public ResponseEntity<FeeStructureResponse> getFeeStructure(@PathVariable Long id) {
        return ResponseEntity.ok(feeStructureService.getFeeStructureById(id));
    }

    @PutMapping("/fee-structures/{id}")
    public ResponseEntity<FeeStructureResponse> updateFeeStructure(
            @PathVariable Long id,
            @Valid @RequestBody FeeStructureRequest request) {
        return ResponseEntity.ok(feeStructureService.updateFeeStructure(id, request));
    }

    @DeleteMapping("/fee-structures/{id}")
    public ResponseEntity<Void> deleteFeeStructure(@PathVariable Long id) {
        feeStructureService.deleteFeeStructure(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fee-structures/term/{termId}")
    public ResponseEntity<List<FeeStructureResponse>> feeStructuresByTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(feeStructureService.getByTerm(termId));
    }

    @GetMapping("/fee-structures/department/{departmentId}")
    public ResponseEntity<List<FeeStructureResponse>> feeStructuresByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(feeStructureService.getByDepartment(departmentId));
    }

    // ---------- Invoices ----------

    @PostMapping("/invoices/generate")
    public ResponseEntity<List<InvoiceResponse>> generateInvoices(
            @Valid @RequestBody GenerateInvoicesRequest request) {
        return new ResponseEntity<>(financeService.generateInvoices(request), HttpStatus.CREATED);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(financeService.getAllInvoices());
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(financeService.getInvoiceById(id));
    }

    @GetMapping("/invoices/term/{termId}")
    public ResponseEntity<List<InvoiceResponse>> invoicesByTerm(@PathVariable Long termId) {
        return ResponseEntity.ok(financeService.getInvoicesByTerm(termId));
    }

    @GetMapping("/invoices/status/{status}")
    public ResponseEntity<List<InvoiceResponse>> invoicesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(financeService.getInvoicesByStatus(status));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceResponse>> overdueInvoices() {
        return ResponseEntity.ok(financeService.getOverdueInvoices());
    }

    // ---------- Payments ----------

    @PostMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<PaymentResponse> recordPayment(
            @PathVariable Long invoiceId,
            @Valid @RequestBody PaymentRequest request) {
        return new ResponseEntity<>(financeService.recordPayment(invoiceId, request), HttpStatus.CREATED);
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<List<PaymentResponse>> getInvoicePayments(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(financeService.getPaymentsForInvoice(invoiceId));
    }
}