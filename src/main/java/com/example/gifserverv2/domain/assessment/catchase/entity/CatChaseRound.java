package com.example.gifserverv2.domain.assessment.catchase.entity;

import com.example.gifserverv2.domain.assessment.common.AssessmentReward;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 고양이 술래잡기 검사 1회. 한 라운드 안에 questionCount 개의 문항(CatChaseQuestion)이 순서대로 출제된다.
 */
@Entity
@Table(name = "cat_chase_round")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatChaseRound {

    public static final int GRID_SIZE = 36;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 문항 수 도입 이전 라운드는 0 (랭킹 집계 대상 아님)
    @Column(name = "question_count", nullable = false, columnDefinition = "int default 0")
    private int questionCount;

    @Column(name = "answered_count", nullable = false, columnDefinition = "int default 0")
    private int answeredCount;

    @Column(name = "correct_count", nullable = false, columnDefinition = "int default 0")
    private int correctCount;

    // 모든 문항에 응답했는지 여부. 중간 이탈이면 false
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean completed;

    @Column(name = "coin_rewarded", nullable = false, columnDefinition = "boolean default false")
    private boolean coinRewarded;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime finishedAt;

    private CatChaseRound(Long userId, int questionCount) {
        this.userId = userId;
        this.questionCount = questionCount;
    }

    public static CatChaseRound start(Long userId, int questionCount) {
        return new CatChaseRound(userId, questionCount);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isInProgress() {
        return finishedAt == null;
    }

    public boolean isAllAnswered() {
        return answeredCount >= questionCount;
    }

    public void recordAnswer(boolean correct) {
        this.answeredCount++;
        if (correct) {
            this.correctCount++;
        }
    }

    public void complete(LocalDateTime now) {
        this.finishedAt = now;
        this.completed = true;
    }

    public void abandon(LocalDateTime now) {
        this.finishedAt = now;
        this.completed = false;
    }

    public void markCoinRewarded() {
        this.coinRewarded = true;
    }

    public int calculateCoin() {
        return AssessmentReward.calculate(completed, correctCount);
    }

    /** 실제 지급된 코인. 이탈한 라운드는 0 */
    public int getEarnedCoin() {
        return coinRewarded ? AssessmentReward.calculate(true, correctCount) : 0;
    }
}
