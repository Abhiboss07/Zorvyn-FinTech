package com.zorvyn.fintech.controller;

import com.zorvyn.fintech.dto.request.CreateTransactionRequest;
import com.zorvyn.fintech.dto.response.ApiResponse;
import com.zorvyn.fintech.dto.response.PagedResponse;
import com.zorvyn.fintech.dto.response.TransactionResponse;
import com.zorvyn.fintech.security.SecurityUtils;
import com.zorvyn.fintech.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction processing endpoints")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WRITE')")
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @Valid @RequestBody CreateTransactionRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = SecurityUtils.getCurrentUserId();
        TransactionResponse response = transactionService.createTransaction(
                userId, request, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "List transactions (filtered by user for non-admins)")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = SecurityUtils.isAdmin() ? null : SecurityUtils.getCurrentUserId();
        Page<TransactionResponse> result = transactionService.listTransactions(userId, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        PagedResponse<TransactionResponse> paged = new PagedResponse<>(
                result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.isLast());
        return ResponseEntity.ok(ApiResponse.success(paged));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('READ')")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getTransaction(id)));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('APPROVE')")
    @Operation(summary = "Approve a pending transaction (finance_manager+)")
    public ResponseEntity<ApiResponse<TransactionResponse>> approve(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        UUID approverId = SecurityUtils.getCurrentUserId();
        TransactionResponse response = transactionService.approveTransaction(
                id, approverId, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Transaction approved", response));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('APPROVE')")
    @Operation(summary = "Reject a pending transaction (finance_manager+)")
    public ResponseEntity<ApiResponse<TransactionResponse>> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest httpRequest) {
        UUID rejecterId = SecurityUtils.getCurrentUserId();
        String reason = body != null ? body.get("reason") : null;
        TransactionResponse response = transactionService.rejectTransaction(
                id, rejecterId, reason, getIp(httpRequest), getUserAgent(httpRequest));
        return ResponseEntity.ok(ApiResponse.success("Transaction rejected", response));
    }

    private String getIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest req) {
        return req.getHeader("User-Agent");
    }
}
