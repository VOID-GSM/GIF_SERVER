package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CatChaseRoundDetailResponse(
        Long roundId,
        int questionCount,
        int answeredCount,
        int correctCount,
        boolean completed,
        int earnedCoin,
        List<QuestionResult> questions,
        LocalDateTime createdAt,
        LocalDateTime finishedAt
) {
    public record QuestionResult(
            Long questionId,
            int sequence,
            List<Integer> mousePositions,
            List<Integer> catPositions,
            boolean answered,
            boolean correct,
            List<JudgmentResult> responses
    ) {
        public static QuestionResult from(CatChaseQuestion question, List<CatChaseJudgment> judgments) {
            return new QuestionResult(
                    question.getId(),
                    question.getSequence(),
                    List.copyOf(question.getMousePositions()),
                    List.copyOf(question.getCatPositions()),
                    question.isAnswered(),
                    question.isCorrect(),
                    judgments.stream().map(JudgmentResult::from).toList()
            );
        }
    }

    public record JudgmentResult(
            int position,
            Judgment judgment,
            ConfidenceLevel confidence,
            boolean hadMouse,
            boolean correct
    ) {
        public static JudgmentResult from(CatChaseJudgment judgment) {
            return new JudgmentResult(
                    judgment.getPosition(),
                    judgment.getJudgment(),
                    judgment.getConfidence(),
                    judgment.isHadMouse(),
                    judgment.isCorrect()
            );
        }
    }

    public static CatChaseRoundDetailResponse from(CatChaseRound round, List<CatChaseQuestion> questions,
                                                   Map<Long, List<CatChaseJudgment>> judgmentsByQuestion) {
        return new CatChaseRoundDetailResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getAnsweredCount(),
                round.getCorrectCount(),
                round.isCompleted(),
                round.getEarnedCoin(),
                questions.stream()
                        .map(q -> QuestionResult.from(q, judgmentsByQuestion.getOrDefault(q.getId(), List.of())))
                        .toList(),
                round.getCreatedAt(),
                round.getFinishedAt()
        );
    }
}
