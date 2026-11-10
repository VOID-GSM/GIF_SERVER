package com.example.gifserverv2.domain.coin.repository;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoinAccountRepository extends JpaRepository<CoinAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CoinAccount> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CoinAccount a where a.userId = :userId")
    Optional<CoinAccount> findByUserIdWithLock(@Param("userId") Long userId);
}