package com.example.gifserverv2.domain.assessment.roadmaking.dto.response;

import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingProblem;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Person;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Vehicle;

import java.util.List;

public record RoadMakingProblemResponse(
        Long problemId,
        int sequence,
        int rows,
        int cols,
        int minFenceCount,
        List<Vehicle> vehicles,
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