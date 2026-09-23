package com.example.gifserverv2.domain.coin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coin_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoinAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int balance;

    private CoinAccount(Long userId) {
        this.userId = userId;
        this.balance = 0;
    }

    public static CoinAccount open(Long userId) {
        return new CoinAccount(userId);
    }

    public void addCoin(int amount) {
        this.balance += amount;
    }

    public void deductCoin(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("차감할 코인은 양수여야 합니다.");
        }
        if (this.balance < amount) {
            throw new IllegalStateException("코인 잔액이 부족합니다.");
        }
        this.balance -= amount;
    }
}