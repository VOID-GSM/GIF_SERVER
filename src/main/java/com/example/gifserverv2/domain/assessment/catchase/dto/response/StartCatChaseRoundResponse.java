package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "고양이 술래잡기 라운드 시작 결과")
public record StartCatChaseRoundResponse(
        @Schema(description = "라운드 ID", example = "11")
        Long roundId,
        @Schema(description = "격자 칸 수", example = "36")
        int gridSize,
        @Schema(description = "전체 문항 수", example = "10")
        int questionCount,
        @Schema(description = "첫 문항")
        CatChaseQuestionResponse question
) {
    public static StartCatChaseRoundResponse of(CatChaseRound round, CatChaseQuestionResponse question) {
        return new StartCatChaseRoundResponse(round.getId(), CatChaseRound.GRID_SIZE, round.getQuestionCount(), question);
    }
}
