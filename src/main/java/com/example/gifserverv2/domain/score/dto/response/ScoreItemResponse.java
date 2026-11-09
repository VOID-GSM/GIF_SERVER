package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.ScoreItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "평가 항목")
public record ScoreItemResponse(
        @Schema(description = "항목 ID", example = "4")
        Long id,
        @Schema(description = "영역 ID", example = "1")
        Long areaId,
        @Schema(description = "항목 이름", example = "기술 완성도")
        String name,
        @Schema(description = "평가 기준 설명. 없으면 null", example = "핵심 기능이 동작하는가")
        String criteria,
        @Schema(description = "최고점 (가장 큰 선택지)", example = "40")
        int maxScore,
        @Schema(description = "선택지 점수", example = "[40, 32, 24, 16, 8]")
        List<Integer> options,
        @Schema(description = "표시 순서", example = "0")
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
