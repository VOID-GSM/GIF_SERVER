package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Color;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "길 만들기 제출 결과")
public record SubmitRoadMakingAnswerResponse(
        @Schema(description = "정답 여부 (모든 차량이 같은 색 사람에게 도착)", example = "true")
        boolean correct,
        @Schema(description = "제한 시간이 지나 채점하지 않았는지", example = "false")
        boolean timeOver,
        @Schema(description = "지금까지 맞힌 문항 수", example = "3")
        int solvedCount,
        @Schema(description = "지금까지 틀린 문항 수", example = "1")
        int wrongCount,
        @Schema(description = "차량별 이동 결과. timeOver면 빈 배열")
        List<VehicleResult> vehicleResults,
        @Schema(description = "다음 문항. 마지막 문항이었거나 시간 초과면 null")
        RoadMakingProblemResponse nextProblem,
        @Schema(description = "라운드 결과. 라운드가 끝나지 않았으면 null")
        RoadMakingRoundResultResponse result
) {
    @Schema(description = "차량별 이동 결과")
    public record VehicleResult(
            @Schema(description = "차량 색", example = "RED")
            Color color,
            @Schema(description = "이동 경로 (칸 위치 순서)")
            List<Position> path,
            @Schema(description = "같은 색 사람에게 도착했는지", example = "true")
            boolean reached
    ) {
    }

    public static SubmitRoadMakingAnswerResponse of(boolean correct, RoadMakingRound round,
                                                    List<VehicleResult> vehicleResults, RoadMakingProblemResponse nextProblem,
                                                    RoadMakingRoundResultResponse result) {
        return new SubmitRoadMakingAnswerResponse(correct, false, round.getSolvedCount(), round.getWrongCount(),
                vehicleResults, nextProblem, result);
    }

    public static SubmitRoadMakingAnswerResponse timeOver(RoadMakingRound round, RoadMakingRoundResultResponse result) {
        return new SubmitRoadMakingAnswerResponse(false, true, round.getSolvedCount(), round.getWrongCount(),
                List.of(), null, result);
    }
}