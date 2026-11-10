package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "보고서 점수 수정 요청. 모든 값 필수")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchReportScoreRequest {
    @Schema(description = "보고서 - 작성", example = "20")
    private Integer reportWriting;
    @Schema(description = "보고서 - 내용", example = "20")
    private Integer reportContent;
    @Schema(description = "보고서 - AI 활용 계획", example = "15")
    private Integer aiUsagePlan;
    @Schema(description = "보고서 - 창의성", example = "15")
    private Integer creativity;
}
