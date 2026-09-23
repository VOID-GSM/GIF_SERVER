package com.example.gifserverv2.domain.assessment.roadmaking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "road_making_round")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoadMakingRound {

    public static final int BASE_COIN = 20;
    public static final int COIN_PER_SOLVED = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int solvedCount;

    @Column(nullable = false)
    private int wrongCount;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean coinRewarded;

    private RoadMakingRound(Long userId, LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.status = RoundStatus.IN_PROGRESS;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
    }

    public static RoadMakingRound start(Long userId, LocalDateTime now, Duration timeLimit) {
        return new RoadMakingRound(userId, now, now.plus(timeLimit));
    }

    public boolean isInProgress() {
        return status == RoundStatus.IN_PROGRESS;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public int calculateCoin() {
        return BASE_COIN + solvedCount * COIN_PER_SOLVED;
    }

    public void markSolved() {
        this.solvedCount++;
    }

    public void markCoinRewarded() {
        this.coinRewarded = true;
    }

    public void markWrong() {
        this.wrongCount++;
    }

    public void finish(LocalDateTime now) {
        this.status = RoundStatus.FINISHED;
        this.finishedAt = now.isAfter(expiresAt) ? expiresAt : now;
    }
}