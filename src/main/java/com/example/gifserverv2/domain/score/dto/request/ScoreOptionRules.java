package com.example.gifserverv2.domain.score.dto.request;

/**
 * 평가 항목 선택지 점수 제한값. 요청 검증 어노테이션에서 함께 쓴다.
 */
final class ScoreOptionRules {

    static final int MAX_OPTIONS = 10;
    static final int MAX_SCORE = 1000;

    private ScoreOptionRules() {
    }
}
