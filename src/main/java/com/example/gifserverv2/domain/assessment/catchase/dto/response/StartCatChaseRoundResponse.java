package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;

import java.util.List;

public record StartCatChaseRoundResponse(
        Long roundId,
        int gridSize,
        List<Integer> mousePositions,
        List<Integer> catPositions
) {
    public static StartCatChaseRoundResponse from(CatChaseRound round) {
        return new StartCatChaseRoundResponse(
                round.getId(),
                CatChaseRound.GRID_SIZE,
                round.getMousePositions(),
                round.getCatPositions()
        );
    }
}
