package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.ScoreItem;

import java.util.List;

public record ScoreItemResponse(
        Long id,
        Long areaId,
        String name,
        String criteria,
        int maxScore,
        List<Integer> options,
        int displayOrder
) {
    public static ScoreItemResponse from(ScoreItem item) {
        return new ScoreItemResponse(
                item.getId(),
                item.getArea().getId(),
                item.getName(),
                item.getCriteria(),
                item.getMaxScore(),
                List.copyOf(item.getOptions()),
                item.getDisplayOrder());
    }
}
