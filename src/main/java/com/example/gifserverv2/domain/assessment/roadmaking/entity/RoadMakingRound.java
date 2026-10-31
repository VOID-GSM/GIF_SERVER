package com.example.gifserverv2.domain.assessment.roadmaking.entity;

import com.example.gifserverv2.domain.assessment.common.AssessmentReward;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status;

    // 문항 수 도입 이전 라운드는 0 (랭킹 집계 대상 아님)
    @Column(nullable = false, columnDefinition = "int default 0")
    private int questionCount;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime finishedAt;

    @Column(nullable = false)
    private int solvedCount;

    @Column(nullable = false)
    private int wrongCount;

    // 끝까지 진행(전 문항 응답 또는 제한 시간 종료)했는지 여부. 중간 이탈이면 false
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean completed;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean coinRewarded;

    private RoadMakingRound(Long userId, int questionCount, LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.userId = userId;
        this.status = RoundStatus.IN_PROGRESS;
        this.questionCount = questionCount;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
    }

    public static RoadMakingRound start(Long userId, int questionCount, LocalDateTime now, Duration timeLimit) {
        return new RoadMakingRound(userId, questionCount, now, now.plus(timeLimit));
    }

    public boolean isInProgress() {
        return status == RoundStatus.IN_PROGRESS;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public int getAnsweredCount() {
        return solvedCount + wrongCount;
    }

    public boolean isAllAnswered() {
        return getAnsweredCount() >= questionCount;
    }

    /** 실제 지급된(또는 지급될) 코인. 이탈한 라운드는 0 */
    public int getEarnedCoin() {
        return coinRewarded ? AssessmentReward.calculate(true, solvedCount) : 0;
    }

    public int calculateCoin() {
        return AssessmentReward.calculate(completed, solvedCount);
    }

    public void markSolved() {
        this.solvedCount++;
    }

    public void markWrong() {
        this.wrongCount++;
    }

    public void markCoinRewarded() {
        this.coinRewarded = true;
    }

    public void complete(LocalDateTime now) {
        finish(now);
        this.completed = true;
    }

    public void abandon(LocalDateTime now) {
        finish(now);
        this.completed = false;
    }

    private void finish(LocalDateTime now) {
        this.status = RoundStatus.FINISHED;
        this.finishedAt = now.isAfter(expiresAt) ? expiresAt : now;
    }
}
