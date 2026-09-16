package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;

import java.time.LocalDateTime;
import java.util.List;

public record CatChaseRoundDetailResponse(
        Long roundId,
        List<Integer> mousePositions,
        List<Integer> catPositions,
        List<JudgmentResult> responses,
        LocalDateTime createdAt
) {
    public record JudgmentResult(
            int position,
            Judgment judgment,
            ConfidenceLevel confidence,
            boolean hadMouse
    ) {
        public static JudgmentResult from(CatChaseJudgment judgment) {
            return new JudgmentResult(
                    judgment.getPosition(),
                    judgment.getJudgment(),
                    judgment.getConfidence(),
                    judgment.isHadMouse()
            );
        }
    }

    public static CatChaseRoundDetailResponse from(CatChaseRound round, List<CatChaseJudgment> judgments) {
        return new CatChaseRoundDetailResponse(
                round.getId(),
                round.getMousePositions(),
                round.getCatPositions(),
                judgments.stream().map(JudgmentResult::from).toList(),
                round.getCreatedAt()
        );
    }
}
