package com.example.gifserverv2.domain.coin.repository;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface CoinAccountRepository extends JpaRepository<CoinAccount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CoinAccount> findByUserId(Long userId);
}