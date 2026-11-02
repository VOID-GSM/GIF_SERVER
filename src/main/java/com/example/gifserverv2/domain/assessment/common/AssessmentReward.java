package com.example.gifserverv2.domain.assessment.common;

/**
 * 역량검사 코인 보상 규칙: 끝까지 진행하고 1문항 이상 맞히면 기본 20코인 + 맞힌 문항마다 10코인.
 * 모두 틀리거나 중간에 이탈하면 0 (아무렇게나 빠르게 제출해 기본 코인을 반복해서 받는 것을 막기 위함).
 */
public final class AssessmentReward {

    public static final int BASE_COIN = 20;
    public static final int COIN_PER_CORRECT = 10;

    private AssessmentReward() {
    }

    public static int calculate(boolean completed, int correctCount) {
        if (!completed || correctCount <= 0) {
            return 0;
        }
        return BASE_COIN + correctCount * COIN_PER_CORRECT;
    }
}
