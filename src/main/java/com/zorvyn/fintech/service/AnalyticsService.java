package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.response.AnalyticsSummaryResponse;
import com.zorvyn.fintech.dto.response.AnalyticsSummaryResponse.PeriodStats;
import com.zorvyn.fintech.dto.response.AnalyticsSummaryResponse.TypeBreakdown;
import com.zorvyn.fintech.repository.AccountRepository;
import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.util.Constants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AnalyticsService(TransactionRepository transactionRepository,
                             UserRepository userRepository,
                             AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Cacheable(value = "analytics", key = "'summary'")
    public AnalyticsSummaryResponse getSummary() {
        AnalyticsSummaryResponse resp = new AnalyticsSummaryResponse();

        // Transaction counts by status
        long pending = transactionRepository.countByStatus(Constants.TXN_STATUS_PENDING);
        long completed = transactionRepository.countByStatus(Constants.TXN_STATUS_COMPLETED);
        long rejected = transactionRepository.countByStatus(Constants.TXN_STATUS_REJECTED);
        long total = pending + completed + rejected;

        resp.setTotalTransactions(total);
        resp.setPendingTransactions(pending);
        resp.setCompletedTransactions(completed);
        resp.setRejectedTransactions(rejected);

        // Total volume (completed only)
        BigDecimal totalVolume = transactionRepository.sumAmountByStatus(Constants.TXN_STATUS_COMPLETED);
        resp.setTotalVolume(totalVolume);

        // Users and accounts
        resp.setActiveUsers(userRepository.countByIsActiveTrue());
        resp.setActiveAccounts(accountRepository.countActiveAccounts());
        resp.setTotalBalance(accountRepository.sumAllActiveBalances());

        // Breakdown by type
        Map<String, TypeBreakdown> byType = new LinkedHashMap<>();
        for (String type : new String[]{Constants.TXN_TYPE_TRANSFER, Constants.TXN_TYPE_DEPOSIT, Constants.TXN_TYPE_WITHDRAWAL}) {
            long count = transactionRepository.countByTypeCompleted(type);
            BigDecimal volume = transactionRepository.sumAmountByTypeCompleted(type);
            byType.put(type, new TypeBreakdown(count, volume));
        }
        resp.setByType(byType);

        // Period comparisons
        Instant now = Instant.now();
        Map<String, PeriodStats> byPeriod = new LinkedHashMap<>();
        byPeriod.put("24h", buildPeriodStats(now.minus(Duration.ofHours(24))));
        byPeriod.put("7d", buildPeriodStats(now.minus(Duration.ofDays(7))));
        byPeriod.put("30d", buildPeriodStats(now.minus(Duration.ofDays(30))));
        resp.setByPeriod(byPeriod);

        return resp;
    }

    private PeriodStats buildPeriodStats(Instant since) {
        long count = transactionRepository.countByStatusSince(Constants.TXN_STATUS_COMPLETED, since);
        BigDecimal volume = transactionRepository.sumAmountByStatusSince(Constants.TXN_STATUS_COMPLETED, since);
        return new PeriodStats(count, volume);
    }
}
