package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.request.CreateTransactionRequest;
import com.zorvyn.fintech.dto.response.TransactionResponse;
import com.zorvyn.fintech.entity.Account;
import com.zorvyn.fintech.entity.Transaction;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.exception.InsufficientBalanceException;
import com.zorvyn.fintech.exception.ResourceNotFoundException;
import com.zorvyn.fintech.repository.AccountRepository;
import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.util.Constants;
import com.zorvyn.fintech.validator.ComplianceChecker;
import com.zorvyn.fintech.validator.TransactionValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final AuditService auditService;
    private final TransactionValidator transactionValidator;
    private final ComplianceChecker complianceChecker;

    private final AtomicLong refCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    public TransactionService(TransactionRepository transactionRepository,
                               AccountRepository accountRepository,
                               UserRepository userRepository,
                               EncryptionService encryptionService,
                               AuditService auditService,
                               TransactionValidator transactionValidator,
                               ComplianceChecker complianceChecker) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.auditService = auditService;
        this.transactionValidator = transactionValidator;
        this.complianceChecker = complianceChecker;
    }

    @Transactional
    public TransactionResponse createTransaction(UUID userId, CreateTransactionRequest request,
                                                  String ipAddress, String userAgent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate transaction type-specific requirements
        transactionValidator.validate(request);

        // Resolve accounts
        Account fromAccount = null;
        Account toAccount = null;

        if (request.getFromAccountId() != null) {
            fromAccount = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getFromAccountId()));

            // Check balance
            if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance in source account");
            }
        }

        if (request.getToAccountId() != null) {
            toAccount = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.getToAccountId()));
        }

        // Check limits
        transactionValidator.checkLimits(userId, request.getAmount(), transactionRepository);

        // Run compliance checks
        complianceChecker.check(userId, request.getAmount(), transactionRepository);

        // Build transaction
        Transaction txn = new Transaction();
        txn.setUser(user);
        txn.setFromAccount(fromAccount);
        txn.setToAccount(toAccount);
        txn.setAmount(request.getAmount());
        txn.setType(request.getType());
        txn.setStatus(Constants.TXN_STATUS_PENDING);
        txn.setReferenceNumber(generateReferenceNumber());

        // Encrypt sensitive details
        if (request.getDetails() != null && !request.getDetails().isBlank()) {
            txn.setEncryptedDetails(encryptionService.encrypt(request.getDetails()));
        }

        txn = transactionRepository.save(txn);

        // Audit log
        auditService.log(userId.toString(), Constants.AUDIT_TRANSACTION_CREATE, "transaction",
                txn.getId().toString(),
                Map.of("amount", request.getAmount(), "type", request.getType(), "ref", txn.getReferenceNumber()),
                ipAddress, userAgent);

        return TransactionResponse.fromEntity(txn);
    }

    @Transactional
    public TransactionResponse approveTransaction(UUID txnId, UUID approverId,
                                                   String ipAddress, String userAgent) {
        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", txnId));

        if (!Constants.TXN_STATUS_PENDING.equals(txn.getStatus())) {
            throw new com.zorvyn.fintech.exception.ApiException("Transaction is not pending",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", approverId));

        // Execute fund movement
        if (txn.getFromAccount() != null) {
            Account from = txn.getFromAccount();
            from.setBalance(from.getBalance().subtract(txn.getAmount()));
            accountRepository.save(from);
        }
        if (txn.getToAccount() != null) {
            Account to = txn.getToAccount();
            to.setBalance(to.getBalance().add(txn.getAmount()));
            accountRepository.save(to);
        }

        txn.setStatus(Constants.TXN_STATUS_COMPLETED);
        txn.setApprovedBy(approver);
        txn.setApprovedAt(Instant.now());
        txn = transactionRepository.save(txn);

        auditService.log(approverId.toString(), Constants.AUDIT_TRANSACTION_APPROVE, "transaction",
                txnId.toString(), Map.of("amount", txn.getAmount()), ipAddress, userAgent);

        return TransactionResponse.fromEntity(txn);
    }

    @Transactional
    public TransactionResponse rejectTransaction(UUID txnId, UUID rejecterId, String reason,
                                                  String ipAddress, String userAgent) {
        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", txnId));

        if (!Constants.TXN_STATUS_PENDING.equals(txn.getStatus())) {
            throw new com.zorvyn.fintech.exception.ApiException("Transaction is not pending",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        txn.setStatus(Constants.TXN_STATUS_REJECTED);
        txn.setRejectionReason(reason);
        txn = transactionRepository.save(txn);

        auditService.log(rejecterId.toString(), Constants.AUDIT_TRANSACTION_REJECT, "transaction",
                txnId.toString(), Map.of("reason", reason != null ? reason : ""), ipAddress, userAgent);

        return TransactionResponse.fromEntity(txn);
    }

    public TransactionResponse getTransaction(UUID txnId) {
        Transaction txn = transactionRepository.findById(txnId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", txnId));
        return TransactionResponse.fromEntity(txn);
    }

    public Page<TransactionResponse> listTransactions(UUID userId, String status, Pageable pageable) {
        Page<Transaction> page;
        if (userId != null && status != null) {
            page = transactionRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            page = transactionRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            page = transactionRepository.findByStatus(status, pageable);
        } else {
            page = transactionRepository.findAll(pageable);
        }
        return page.map(TransactionResponse::fromEntity);
    }

    private String generateReferenceNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long counter = refCounter.incrementAndGet();
        return String.format("%s%s-%06d", Constants.TXN_REF_PREFIX, date, counter % 1000000);
    }
}
