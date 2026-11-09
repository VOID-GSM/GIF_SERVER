package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "전공 점수 수정 요청. 모든 값 필수")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchMajorScoreRequest {
    @Schema(description = "전공 - 기술 완성도", example = "30")
    private Integer technicalCompleteness;
    @Schema(description = "전공 - 사회적 가치", example = "20")
    private Integer socialValueMajor;
    @Schema(description = "전공 - AI 활용", example = "15")
    private Integer aiUtilityMajorScore;
    @Schema(description = "전공 - 발표", example = "20")
    private Integer presentationMajor;
}
