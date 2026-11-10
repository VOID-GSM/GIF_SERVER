package com.example.gifserverv2.domain.score.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "프로젝트 영역별 평균 (반올림 정수)")
@Getter
@AllArgsConstructor
public class GetProjectFieldAverageResponse {
    @Schema(description = "프로젝트 ID", example = "1")
    private Long projectId;
    @Schema(description = "전공 영역 합계의 평균", example = "82")
    private int majorAverage;
    @Schema(description = "보고서 영역 합계의 평균", example = "64")
    private int reportAverage;
    @Schema(description = "사회 영역 합계의 평균", example = "70")
    private int communityAverage;
    @Schema(description = "세 영역 평균의 평균", example = "72")
    private int grandTotalAverage;
}