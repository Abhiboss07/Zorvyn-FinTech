package com.zorvyn.fintech.validator;

import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Compliance checks: AML screening and fraud detection.
 */
@Component
public class ComplianceChecker {

    private static final Logger log = LoggerFactory.getLogger(ComplianceChecker.class);

    /**
     * Run compliance checks. Logs warnings for flagged transactions
     * but does not block them (in production, these would trigger reviews).
     */
    public void check(UUID userId, BigDecimal amount, TransactionRepository txnRepo) {
        checkAml(userId, amount);
        checkVelocity(userId, txnRepo);
    }

    /**
     * AML: Flag transactions >= $10,000 (Bank Secrecy Act threshold).
     */
    private void checkAml(UUID userId, BigDecimal amount) {
        if (amount.compareTo(Constants.AML_THRESHOLD) >= 0) {
            log.warn("AML FLAG | userId={} | amount=${} | Transaction meets BSA reporting threshold",
                    userId, amount.toPlainString());
        }
    }

    /**
     * Fraud: Velocity check — flag if > 5 transactions in last hour.
     */
    private void checkVelocity(UUID userId, TransactionRepository txnRepo) {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentCount = txnRepo.countByUserIdSince(userId, oneHourAgo);

        if (recentCount >= Constants.VELOCITY_MAX_TRANSACTIONS_PER_HOUR) {
            log.warn("FRAUD FLAG | userId={} | {} transactions in the past hour (threshold: {})",
                    userId, recentCount, Constants.VELOCITY_MAX_TRANSACTIONS_PER_HOUR);
        }
    }
}
