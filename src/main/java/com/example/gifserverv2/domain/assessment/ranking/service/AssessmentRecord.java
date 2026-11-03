package com.example.gifserverv2.domain.assessment.ranking.service;

import java.time.LocalDateTime;

/**
 * 랭킹 계산에 쓰이는 완료된 검사 1회분의 기록.
 */
public record AssessmentRecord(
        Long userId,
        int correctCount,
        int totalCount,
        long elapsedSeconds,
        LocalDateTime achievedAt,
        int earnedCoin
) {
}
