package com.example.gifserverv2.domain.score.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사회 점수 수정 요청. 모든 값 필수")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchSocialScoreRequest {
    @Schema(description = "사회 - 사용자 경험", example = "20")
    private Integer userExperience;
    @Schema(description = "사회 - 사회적 가치", example = "20")
    private Integer socialValueCommunity;
    @Schema(description = "사회 - AI 활용", example = "15")
    private Integer aiUtilizationCommunity;
    @Schema(description = "사회 - 발표", example = "15")
    private Integer presentationCommunity;
}
