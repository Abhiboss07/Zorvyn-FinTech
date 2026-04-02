package com.zorvyn.fintech.validator;

import com.zorvyn.fintech.dto.request.CreateTransactionRequest;
import com.zorvyn.fintech.exception.TransactionLimitException;
import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.util.Constants;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Business rule validation for transactions.
 */
@Component
public class TransactionValidator {

    public void validate(CreateTransactionRequest request) {
        switch (request.getType()) {
            case Constants.TXN_TYPE_TRANSFER -> {
                if (request.getFromAccountId() == null || request.getToAccountId() == null) {
                    throw new IllegalArgumentException("Transfer requires both fromAccountId and toAccountId");
                }
            }
            case Constants.TXN_TYPE_DEPOSIT -> {
                if (request.getToAccountId() == null) {
                    throw new IllegalArgumentException("Deposit requires toAccountId");
                }
            }
            case Constants.TXN_TYPE_WITHDRAWAL -> {
                if (request.getFromAccountId() == null) {
                    throw new IllegalArgumentException("Withdrawal requires fromAccountId");
                }
            }
        }
    }

    /**
     * Check transaction limits: $10,000 per single transaction, $50,000 daily.
     */
    public void checkLimits(UUID userId, BigDecimal amount, TransactionRepository txnRepo) {
        // Single transaction limit
        if (amount.compareTo(Constants.SINGLE_TRANSACTION_LIMIT) > 0) {
            throw new TransactionLimitException(
                    String.format("Single transaction limit is $%s. Requested: $%s",
                            Constants.SINGLE_TRANSACTION_LIMIT.toPlainString(), amount.toPlainString()));
        }

        // Daily limit
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        BigDecimal dailyTotal = txnRepo.sumDailyAmountByUserId(userId, startOfDay);
        if (dailyTotal.add(amount).compareTo(Constants.DAILY_TRANSACTION_LIMIT) > 0) {
            throw new TransactionLimitException(
                    String.format("Daily transaction limit is $%s. Today's total with this would be: $%s",
                            Constants.DAILY_TRANSACTION_LIMIT.toPlainString(),
                            dailyTotal.add(amount).toPlainString()));
        }
    }
}
