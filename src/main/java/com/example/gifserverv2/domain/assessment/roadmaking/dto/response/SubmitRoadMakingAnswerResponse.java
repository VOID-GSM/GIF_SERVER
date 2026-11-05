package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Color;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;

import java.util.List;

public record SubmitRoadMakingAnswerResponse(
        boolean correct,
        boolean timeOver,
        int solvedCount,
        int wrongCount,
        List<VehicleResult> vehicleResults,
        RoadMakingProblemResponse nextProblem,
        RoadMakingRoundResultResponse result
) {
    public record VehicleResult(Color color, List<Position> path, boolean reached) {
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