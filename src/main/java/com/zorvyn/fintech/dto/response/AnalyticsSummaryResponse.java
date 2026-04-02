package com.zorvyn.fintech.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class AnalyticsSummaryResponse {

    private long totalTransactions;
    private BigDecimal totalVolume;
    private long pendingTransactions;
    private long completedTransactions;
    private long rejectedTransactions;
    private long activeUsers;
    private long activeAccounts;
    private BigDecimal totalBalance;
    private Map<String, TypeBreakdown> byType;
    private Map<String, PeriodStats> byPeriod;

    // ── Inner classes ──

    public static class TypeBreakdown {
        private long count;
        private BigDecimal volume;

        public TypeBreakdown(long count, BigDecimal volume) {
            this.count = count;
            this.volume = volume;
        }

        public long getCount() { return count; }
        public BigDecimal getVolume() { return volume; }
    }

    public static class PeriodStats {
        private long transactionCount;
        private BigDecimal volume;

        public PeriodStats(long transactionCount, BigDecimal volume) {
            this.transactionCount = transactionCount;
            this.volume = volume;
        }

        public long getTransactionCount() { return transactionCount; }
        public BigDecimal getVolume() { return volume; }
    }

    // ── Getters & Setters ──

    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getTotalVolume() { return totalVolume; }
    public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }

    public long getPendingTransactions() { return pendingTransactions; }
    public void setPendingTransactions(long pendingTransactions) { this.pendingTransactions = pendingTransactions; }

    public long getCompletedTransactions() { return completedTransactions; }
    public void setCompletedTransactions(long completedTransactions) { this.completedTransactions = completedTransactions; }

    public long getRejectedTransactions() { return rejectedTransactions; }
    public void setRejectedTransactions(long rejectedTransactions) { this.rejectedTransactions = rejectedTransactions; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getActiveAccounts() { return activeAccounts; }
    public void setActiveAccounts(long activeAccounts) { this.activeAccounts = activeAccounts; }

    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }

    public Map<String, TypeBreakdown> getByType() { return byType; }
    public void setByType(Map<String, TypeBreakdown> byType) { this.byType = byType; }

    public Map<String, PeriodStats> getByPeriod() { return byPeriod; }
    public void setByPeriod(Map<String, PeriodStats> byPeriod) { this.byPeriod = byPeriod; }
}
