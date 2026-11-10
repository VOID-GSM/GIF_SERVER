package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.entity.ConfidenceLevel;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "고양이 술래잡기 라운드 상세")
public record CatChaseRoundDetailResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "응답한 문항 수", example = "10")
        int answeredCount,
        @Schema(description = "맞힌 문항 수", example = "7")
        int correctCount,
        @Schema(description = "끝까지 진행했는지", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin,
        @Schema(description = "문항별 결과")
        List<QuestionResult> questions,
        @Schema(description = "시작 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "종료 일시. 진행 중이면 null", example = "2026-11-09T10:05:00")
        LocalDateTime finishedAt
) {
    @Schema(description = "문항별 결과")
    public record QuestionResult(
            @Schema(description = "문항 ID", example = "51")
            Long questionId,
            @Schema(description = "문항 순서", example = "1")
            int sequence,
            @Schema(description = "쥐 칸 번호", example = "[3, 14, 20]")
            List<Integer> mousePositions,
            @Schema(description = "고양이 칸 번호. [0] = 빨간 고양이, [1] = 파란 고양이", example = "[14, 30]")
            List<Integer> catPositions,
            @Schema(description = "응답 여부", example = "true")
            boolean answered,
            @Schema(description = "정답 여부", example = "true")
            boolean correct,
            @Schema(description = "내 판단 목록")
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

    @Schema(description = "고양이 칸 하나에 대한 내 판단과 채점 결과")
    public record JudgmentResult(
            @Schema(description = "고양이 칸 번호", example = "14")
            int position,
            @Schema(description = "내 판단. FOUND / MISSED / TIMEOUT(응답 시간 초과)", example = "FOUND")
            Judgment judgment,
            @Schema(description = "확신도. TIMEOUT 이면 null", example = "CONFIDENT", nullable = true)
            ConfidenceLevel confidence,
            @Schema(description = "실제로 쥐가 있었는지", example = "true")
            boolean hadMouse,
            @Schema(description = "판단이 맞았는지", example = "true")
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
