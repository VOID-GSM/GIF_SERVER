package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingProblem;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "길 만들기 문항")
public record RoadMakingProblemResponse(
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
        @Schema(description = "사람 목록 (색·위치). 같은 색 차량이 도착해야 한다")
        List<Person> people
) {
    public static RoadMakingProblemResponse of(RoadMakingProblem problem, Board board) {
        return new RoadMakingProblemResponse(
                problem.getId(),
                problem.getSequence(),
                board.rows(),
                board.cols(),
                problem.getMinFenceCount(),
                board.vehicles(),
                board.people()
        );
    }
}