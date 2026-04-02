package com.zorvyn.fintech.service;

import com.zorvyn.fintech.dto.request.CreateTransactionRequest;
import com.zorvyn.fintech.dto.response.TransactionResponse;
import com.zorvyn.fintech.entity.Account;
import com.zorvyn.fintech.entity.Role;
import com.zorvyn.fintech.entity.Transaction;
import com.zorvyn.fintech.entity.User;
import com.zorvyn.fintech.exception.InsufficientBalanceException;
import com.zorvyn.fintech.exception.ResourceNotFoundException;
import com.zorvyn.fintech.repository.AccountRepository;
import com.zorvyn.fintech.repository.TransactionRepository;
import com.zorvyn.fintech.repository.UserRepository;
import com.zorvyn.fintech.validator.ComplianceChecker;
import com.zorvyn.fintech.validator.TransactionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private EncryptionService encryptionService;
    @Mock private AuditService auditService;
    @Mock private TransactionValidator transactionValidator;
    @Mock private ComplianceChecker complianceChecker;

    private TransactionService transactionService;

    private User testUser;
    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, accountRepository,
                userRepository, encryptionService, auditService, transactionValidator, complianceChecker);

        Role role = new Role();
        role.setName("user");
        role.setPermissions(List.of("read", "write"));

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("user@zorvyn.com");
        testUser.setRole(role);

        fromAccount = new Account();
        fromAccount.setId(UUID.randomUUID());
        fromAccount.setBalance(new BigDecimal("5000.00"));
        fromAccount.setUser(testUser);

        toAccount = new Account();
        toAccount.setId(UUID.randomUUID());
        toAccount.setBalance(new BigDecimal("1000.00"));
        toAccount.setUser(testUser);
    }

    @Test
    void createTransaction_transfer_success() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType("transfer");
        request.setAmount(new BigDecimal("500.00"));
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccount.getId())).thenReturn(Optional.of(toAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction txn = inv.getArgument(0);
            txn.setId(UUID.randomUUID());
            return txn;
        });

        TransactionResponse response = transactionService.createTransaction(
                testUser.getId(), request, "127.0.0.1", "TestAgent");

        assertNotNull(response);
        assertEquals("transfer", response.getType());
        assertEquals("pending", response.getStatus());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        verify(transactionValidator).validate(request);
        verify(complianceChecker).check(eq(testUser.getId()), eq(new BigDecimal("500.00")), eq(transactionRepository));
    }

    @Test
    void createTransaction_insufficientBalance_throws() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType("transfer");
        request.setAmount(new BigDecimal("10000.00"));
        request.setFromAccountId(fromAccount.getId());
        request.setToAccountId(toAccount.getId());

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(accountRepository.findById(fromAccount.getId())).thenReturn(Optional.of(fromAccount));

        assertThrows(InsufficientBalanceException.class,
                () -> transactionService.createTransaction(testUser.getId(), request, "127.0.0.1", "TestAgent"));
    }

    @Test
    void createTransaction_userNotFound_throws() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType("deposit");
        request.setAmount(new BigDecimal("100.00"));

        UUID fakeUserId = UUID.randomUUID();
        when(userRepository.findById(fakeUserId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.createTransaction(fakeUserId, request, "127.0.0.1", "TestAgent"));
    }

    @Test
    void approveTransaction_success() {
        UUID approverId = UUID.randomUUID();
        Role managerRole = new Role();
        managerRole.setName("finance_manager");
        managerRole.setPermissions(List.of("read", "write", "approve"));

        User approver = new User();
        approver.setId(approverId);
        approver.setRole(managerRole);

        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID());
        txn.setUser(testUser);
        txn.setFromAccount(fromAccount);
        txn.setToAccount(toAccount);
        txn.setAmount(new BigDecimal("500.00"));
        txn.setType("transfer");
        txn.setStatus("pending");
        txn.setReferenceNumber("TXN-TEST-001");

        when(transactionRepository.findById(txn.getId())).thenReturn(Optional.of(txn));
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approver));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.approveTransaction(
                txn.getId(), approverId, "127.0.0.1", "TestAgent");

        assertEquals("completed", response.getStatus());
        // Verify balance changes
        assertEquals(new BigDecimal("4500.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("1500.00"), toAccount.getBalance());
    }

    @Test
    void rejectTransaction_success() {
        UUID rejecterId = UUID.randomUUID();

        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID());
        txn.setUser(testUser);
        txn.setAmount(new BigDecimal("500.00"));
        txn.setType("transfer");
        txn.setStatus("pending");
        txn.setReferenceNumber("TXN-TEST-002");

        when(transactionRepository.findById(txn.getId())).thenReturn(Optional.of(txn));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.rejectTransaction(
                txn.getId(), rejecterId, "Suspicious activity", "127.0.0.1", "TestAgent");

        assertEquals("rejected", response.getStatus());
        assertEquals("Suspicious activity", response.getRejectionReason());
    }
}
