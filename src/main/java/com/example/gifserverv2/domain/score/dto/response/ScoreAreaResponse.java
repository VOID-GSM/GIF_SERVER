package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Comparator;
import java.util.List;

@Schema(description = "평가 영역")
public record ScoreAreaResponse(
        @Schema(description = "영역 ID", example = "1")
        Long id,
        @Schema(description = "영역 이름", example = "기술")
        String name,
        @Schema(description = "표시 순서", example = "0")
        int displayOrder,
        @Schema(description = "영역 내 항목 최고점 합계", example = "40")
        int totalMaxScore,
        @Schema(description = "항목 목록 (표시 순서대로)")
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
