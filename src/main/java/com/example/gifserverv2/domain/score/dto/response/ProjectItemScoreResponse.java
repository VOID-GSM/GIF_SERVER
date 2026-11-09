package com.example.gifserverv2.domain.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 프로젝트 하나에 대한 로그인한 선생님의 항목별 채점 현황. 삭제되지 않은 영역/항목만 담는다.
 * 채점하지 않은 항목은 score 가 null 이다.
 */
@Schema(description = "로그인한 선생님의 항목별 채점 현황")
public record ProjectItemScoreResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "내가 준 점수 합계", example = "72")
        int totalScore,
        @Schema(description = "전체 최고점 합계", example = "100")
        int totalMaxScore,
        @Schema(description = "영역 목록")
        List<Area> areas
) {
    @Schema(description = "평가 영역별 채점 현황")
    public record Area(
            @Schema(description = "영역 ID", example = "1")
            Long id,
            @Schema(description = "영역 이름", example = "기술")
            String name,
            @Schema(description = "영역 내 내가 준 점수 합계 (미채점은 0으로 계산)", example = "32")
            int totalScore,
            @Schema(description = "영역 최고점 합계", example = "40")
            int totalMaxScore,
            @Schema(description = "항목 목록")
            List<Item> items
    ) {
    }

    @Schema(description = "평가 항목별 채점 현황")
    public record Item(
            @Schema(description = "항목 ID", example = "4")
            Long id,
            @Schema(description = "항목 이름", example = "기술 완성도")
            String name,
            @Schema(description = "평가 기준 설명", example = "핵심 기능이 동작하는가")
            String criteria,
            @Schema(description = "최고점 (가장 큰 선택지)", example = "40")
            int maxScore,
            @Schema(description = "선택지 점수", example = "[40, 32, 24, 16, 8]")
            List<Integer> options,
            @Schema(description = "내가 준 점수. 미채점이면 null", example = "32")
            Integer score
    ) {
    }
}
