package com.example.gifserverv2.domain.assessment.common;

/**
 * 역량검사 코인 보상 규칙: 끝까지 진행하면 기본 20코인, 맞힌 문항마다 +10코인. 중간 이탈 시 0.
 */
public final class AssessmentReward {

    public static final int BASE_COIN = 20;
    public static final int COIN_PER_CORRECT = 10;

    private AssessmentReward() {
    }

    public static int calculate(boolean completed, int correctCount) {
        return completed ? BASE_COIN + correctCount * COIN_PER_CORRECT : 0;
    }
}
