package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;

import java.util.Comparator;
import java.util.List;

public record ScoreAreaResponse(
        Long id,
        String name,
        int displayOrder,
        int totalMaxScore,
        List<ScoreItemResponse> items
) {
    public static ScoreAreaResponse from(ScoreArea area) {
        List<ScoreItem> items = area.getItems().stream()
                .sorted(Comparator.comparingInt(ScoreItem::getDisplayOrder).thenComparing(ScoreItem::getId))
                .toList();

        return new ScoreAreaResponse(
                area.getId(),
                area.getName(),
                area.getDisplayOrder(),
                items.stream().mapToInt(ScoreItem::getMaxScore).sum(),
                items.stream().map(ScoreItemResponse::from).toList());
    }
}
