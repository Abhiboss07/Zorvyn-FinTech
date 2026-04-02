package com.zorvyn.fintech.repository;

import com.zorvyn.fintech.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserId(UUID userId);

    List<Account> findByUserIdAndIsActiveTrue(UUID userId);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.isActive = true")
    java.math.BigDecimal sumAllActiveBalances();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.isActive = true")
    long countActiveAccounts();

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId AND a.id = :accountId AND a.isActive = true")
    java.util.Optional<Account> findActiveByIdAndUserId(@Param("accountId") UUID accountId, @Param("userId") UUID userId);
}
