package com.example.gifserverv2.domain.score.dto.response;

import com.example.gifserverv2.domain.score.entity.Score;
import lombok.Getter;

@Getter
public class GetDetailScoreResponse {
    private Long id;
    private String evaluatorId;
    private Integer technicalCompleteness;
    private Integer socialValueMajor;
    private Integer aiUtilizationMajor;
    private Integer presentationMajor;
    private Integer reportWriting;
    private Integer reportContent;
    private Integer aiUsagePlan;
    private Integer creativity;
    private Integer userExperience;
    private Integer socialValueCommunity;
    private Integer aiUtilizationCommunity;
    private Integer presentationCommunity;
    private Integer subTotalScore;
    private int rank;

    public GetDetailScoreResponse(Score s) {
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