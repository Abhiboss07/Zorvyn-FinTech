package com.zorvyn.fintech.dto.response;

import com.zorvyn.fintech.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TransactionResponse {

    private UUID id;
    private UUID userId;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String referenceNumber;
    private UUID approvedBy;
    private Instant approvedAt;
    private String rejectionReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static TransactionResponse fromEntity(Transaction txn) {
        TransactionResponse r = new TransactionResponse();
        r.id = txn.getId();
        r.userId = txn.getUser().getId();
        r.fromAccountId = txn.getFromAccount() != null ? txn.getFromAccount().getId() : null;
        r.toAccountId = txn.getToAccount() != null ? txn.getToAccount().getId() : null;
        r.amount = txn.getAmount();
        r.type = txn.getType();
        r.status = txn.getStatus();
        r.referenceNumber = txn.getReferenceNumber();
        r.approvedBy = txn.getApprovedBy() != null ? txn.getApprovedBy().getId() : null;
        r.approvedAt = txn.getApprovedAt();
        r.rejectionReason = txn.getRejectionReason();
        r.createdAt = txn.getCreatedAt();
        r.updatedAt = txn.getUpdatedAt();
        return r;
    }

    // ── Getters ──

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getReferenceNumber() { return referenceNumber; }
    public UUID getApprovedBy() { return approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public String getRejectionReason() { return rejectionReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
