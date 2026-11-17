package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingProblem;
import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "길 만들기 라운드 상세")
public record RoadMakingRoundDetailResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "맞힌 문항 수", example = "7")
        int solvedCount,
        @Schema(description = "틀린 문항 수", example = "3")
        int wrongCount,
        @Schema(description = "정답률 (%, 소수 첫째 자리)", example = "70.0")
        double accuracy,
        @Schema(description = "소요 시간 (초). 진행 중이면 null", example = "212", nullable = true)
        Long elapsedSeconds,
        @Schema(description = "종료 여부 (완료·이탈 포함). false면 진행 중", example = "true")
        boolean finished,
        @Schema(description = "끝까지 진행했는지. 이탈이면 false", example = "true")
        boolean completed,
        @Schema(description = "지급된 코인", example = "90")
        int earnedCoin,
        @Schema(description = "시작 일시", example = "2026-11-09T10:00:00")
        LocalDateTime startedAt,
        @Schema(description = "종료 일시. 진행 중이면 null", example = "2026-11-09T10:03:32", nullable = true)
        LocalDateTime finishedAt,
        @Schema(description = "문항별 결과 (순서대로)")
        List<ProblemResult> problems
) {
    @Schema(description = "문항별 결과")
    public record ProblemResult(
            @Schema(description = "문항 ID", example = "71")
            Long problemId,
            @Schema(description = "문항 순서 (1부터)", example = "1")
            int sequence,
            @Schema(description = "격자 행 수", example = "6")
            int rows,
            @Schema(description = "격자 열 수", example = "6")
            int cols,
            @Schema(description = "놓아야 하는 울타리 개수", example = "3")
            int minFenceCount,
            @Schema(description = "차량 목록 (색·위치·진행 방향)")
            List<Vehicle> vehicles,
            @Schema(description = "사람 목록 (색·위치)")
            List<Person> people,
            @Schema(description = "응답 여부. 진행 중 라운드의 현재 문항이나 이탈 직전 문항은 false", example = "true")
            boolean answered,
            @Schema(description = "정답 여부", example = "true")
            boolean correct,
            @Schema(description = "제출한 울타리 위치. 응답하지 않았거나 제출 답 저장 이전에 응답한 문항은 null", nullable = true)
            List<Position> submittedFences
    ) {
        public static ProblemResult of(RoadMakingProblem problem, Board board, List<Position> submittedFences) {
            return new ProblemResult(
                    problem.getId(),
                    problem.getSequence(),
                    board.rows(),
                    board.cols(),
                    problem.getMinFenceCount(),
                    board.vehicles(),
                    board.people(),
                    problem.isAnswered(),
                    problem.isSolved(),
                    submittedFences
            );
        }
    }

    public static RoadMakingRoundDetailResponse of(RoadMakingRound round, List<ProblemResult> problems) {
        return new RoadMakingRoundDetailResponse(
                round.getId(),
                round.getQuestionCount(),
                round.getSolvedCount(),
                round.getWrongCount(),
                round.getAccuracy(),
                round.getElapsedSeconds(),
                !round.isInProgress(),
                round.isCompleted(),
                round.getEarnedCoin(),
                round.getStartedAt(),
                round.getFinishedAt(),
                problems
        );
    }
}
