package com.zorvyn.fintech.repository;

import com.zorvyn.fintech.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByUserId(UUID userId, Pageable pageable);

    Page<Transaction> findByStatus(String status, Pageable pageable);

    Page<Transaction> findByUserIdAndStatus(UUID userId, String status, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status AND t.createdAt >= :since")
    long countByStatusSince(@Param("status") String status, @Param("since") Instant since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = :status AND t.createdAt >= :since")
    BigDecimal sumAmountByStatusSince(@Param("status") String status, @Param("since") Instant since);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type AND t.status = 'completed'")
    long countByTypeCompleted(@Param("type") String type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = :type AND t.status = 'completed'")
    BigDecimal sumAmountByTypeCompleted(@Param("type") String type);

    // Compliance: count transactions in last hour for velocity check
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.createdAt >= :since")
    long countByUserIdSince(@Param("userId") UUID userId, @Param("since") Instant since);

    // Compliance: sum daily transactions for limit check
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.createdAt >= :since AND t.status != 'rejected'")
    BigDecimal sumDailyAmountByUserId(@Param("userId") UUID userId, @Param("since") Instant since);
}
