package com.zorvyn.fintech.util;

public final class Constants {

    private Constants() {}

    // ── Transaction Limits ──
    public static final java.math.BigDecimal SINGLE_TRANSACTION_LIMIT = new java.math.BigDecimal("10000.00");
    public static final java.math.BigDecimal DAILY_TRANSACTION_LIMIT = new java.math.BigDecimal("50000.00");

    // ── Compliance ──
    public static final java.math.BigDecimal AML_THRESHOLD = new java.math.BigDecimal("10000.00");
    public static final int VELOCITY_MAX_TRANSACTIONS_PER_HOUR = 5;

    // ── Transaction Types ──
    public static final String TXN_TYPE_TRANSFER = "transfer";
    public static final String TXN_TYPE_DEPOSIT = "deposit";
    public static final String TXN_TYPE_WITHDRAWAL = "withdrawal";

    // ── Transaction Statuses ──
    public static final String TXN_STATUS_PENDING = "pending";
    public static final String TXN_STATUS_APPROVED = "approved";
    public static final String TXN_STATUS_REJECTED = "rejected";
    public static final String TXN_STATUS_COMPLETED = "completed";

    // ── Roles ──
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_FINANCE_MANAGER = "finance_manager";
    public static final String ROLE_ANALYST = "analyst";
    public static final String ROLE_USER = "user";

    // ── Audit Actions ──
    public static final String AUDIT_LOGIN = "LOGIN";
    public static final String AUDIT_LOGOUT = "LOGOUT";
    public static final String AUDIT_REGISTER = "REGISTER";
    public static final String AUDIT_TRANSACTION_CREATE = "TRANSACTION_CREATE";
    public static final String AUDIT_TRANSACTION_APPROVE = "TRANSACTION_APPROVE";
    public static final String AUDIT_TRANSACTION_REJECT = "TRANSACTION_REJECT";
    public static final String AUDIT_2FA_SETUP = "2FA_SETUP";
    public static final String AUDIT_2FA_VERIFY = "2FA_VERIFY";
    public static final String AUDIT_USER_UPDATE = "USER_UPDATE";
    public static final String AUDIT_ROLE_ASSIGN = "ROLE_ASSIGN";

    // ── Session ──
    public static final String SESSION_PREFIX = "session:";

    // ── Reference Number ──
    public static final String TXN_REF_PREFIX = "TXN-";
}
