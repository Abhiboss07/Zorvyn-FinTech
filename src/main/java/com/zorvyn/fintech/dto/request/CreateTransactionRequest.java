package com.zorvyn.fintech.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateTransactionRequest {

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "transfer|deposit|withdrawal", message = "Type must be 'transfer', 'deposit', or 'withdrawal'")
    private String type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private UUID fromAccountId;

    private UUID toAccountId;

    private String details;

    // ── Getters & Setters ──

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }

    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
