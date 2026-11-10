package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "고양이 술래잡기 문항")
public record CatChaseQuestionResponse(
        @Schema(description = "문항 ID", example = "51")
        Long questionId,
        @Schema(description = "문항 순서 (1부터)", example = "1")
        int sequence,
        @Schema(description = "쥐가 있는 칸 번호 (0~35)", example = "[3, 14, 20]")
        List<Integer> mousePositions,
        @Schema(description = "고양이가 있는 칸 번호 (0~35). 이 칸마다 판단을 제출한다", example = "[5, 14, 30]")
        List<Integer> catPositions
) {
    public static CatChaseQuestionResponse from(CatChaseQuestion question) {
        return new CatChaseQuestionResponse(
                question.getId(),
                question.getSequence(),
                List.copyOf(question.getMousePositions()),
                List.copyOf(question.getCatPositions())
        );
    }
}
