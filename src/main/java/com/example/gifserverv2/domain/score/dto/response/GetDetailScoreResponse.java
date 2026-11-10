package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.Score;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "선생님 한 명이 매긴 프로젝트 점수. 채점 전이면 모든 값이 null(rank는 0)")
@Getter
@NoArgsConstructor
public class GetDetailScoreResponse {
    @Schema(description = "점수 ID", example = "31")
    private Long id;
    @Schema(description = "채점한 선생님 userId (문자열)", example = "7")
    private String evaluatorId;
    @Schema(description = "전공 - 기술 완성도", example = "30")
    private Integer technicalCompleteness;
    @Schema(description = "전공 - 사회적 가치", example = "20")
    private Integer socialValueMajor;
    @Schema(description = "전공 - AI 활용", example = "15")
    private Integer aiUtilizationMajor;
    @Schema(description = "전공 - 발표", example = "20")
    private Integer presentationMajor;
    @Schema(description = "보고서 - 작성", example = "20")
    private Integer reportWriting;
    @Schema(description = "보고서 - 내용", example = "20")
    private Integer reportContent;
    @Schema(description = "보고서 - AI 활용 계획", example = "15")
    private Integer aiUsagePlan;
    @Schema(description = "보고서 - 창의성", example = "15")
    private Integer creativity;
    @Schema(description = "사회 - 사용자 경험", example = "20")
    private Integer userExperience;
    @Schema(description = "사회 - 사회적 가치", example = "20")
    private Integer socialValueCommunity;
    @Schema(description = "사회 - AI 활용", example = "15")
    private Integer aiUtilizationCommunity;
    @Schema(description = "사회 - 발표", example = "15")
    private Integer presentationCommunity;
    @Schema(description = "12개 항목 합계", example = "235")
    private Integer subTotalScore;
    @Schema(description = "순위 (현재 계산하지 않아 0)", example = "0")
    private int rank;

    public GetDetailScoreResponse(Score s) {
        if (s != null) {
            this.id = s.getId();
            this.evaluatorId = s.getEvaluatorId();
            this.technicalCompleteness = s.getTechnicalCompleteness();
            this.socialValueMajor = s.getSocialValueMajor();
            this.aiUtilizationMajor = s.getAiUtilizationMajor();
            this.presentationMajor = s.getPresentationMajor();
            this.reportWriting = s.getReportWriting();
            this.reportContent = s.getReportContent();
            this.aiUsagePlan = s.getAiUsagePlan();
            this.creativity = s.getCreativity();
            this.userExperience = s.getUserExperience();
            this.socialValueCommunity = s.getSocialValueCommunity();
            this.aiUtilizationCommunity = s.getAiUtilizationCommunity();
            this.presentationCommunity = s.getPresentationCommunity();
            this.subTotalScore = s.getSubTotalScore();
            this.rank = s.getRank();
        }
    }
}