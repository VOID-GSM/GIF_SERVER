package com.example.gifserverv2.domain.score.dto.response;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;

@Getter
public class GetScoreResponse {
    private String teamName;
    private Integer avgTechnicalCompleteness;
    private Integer avgSocialValueMajor;
    private Integer avgAiUtilityMajorScore;
    private Integer avgPresentationMajor;
    private Integer avgReportWriting;
    private Integer avgReportContent;
    private Integer avgAiUsagePlan;
    private Integer avgCreativity;
    private Integer avgUserExperience;
    private Integer avgSocialValueCommunity;
    private Integer avgAiUtilizationCommunity;
    private Integer avgPresentationCommunity;
    private Integer finalTotalScore;

    public GetScoreResponse (String teamName, Integer technicalCompleteness, Integer socialValueMajor, Integer aiUtilityMajorScore, Integer presentationMajor, Integer reportWriting, Integer reportContent, Integer aiUsagePlan, Integer creativity, Integer userExperience, Integer socialvaluecommunity, Integer aiUtilizationCommunity, Integer presentationCommunity, Integer totalScore) {
        this.teamName = teamName;
        this.avgTechnicalCompleteness = technicalCompleteness;
        this.avgSocialValueMajor = socialValueMajor;
        this.avgAiUtilityMajorScore = aiUtilityMajorScore;
        this.avgPresentationMajor = presentationMajor;
        this.avgReportWriting = reportWriting;
        this.avgReportContent = reportContent;
        this.avgAiUsagePlan = aiUsagePlan;
        this.avgCreativity = creativity;
        this.avgUserExperience = userExperience;
        this.avgSocialValueCommunity = socialvaluecommunity;
        this.avgAiUtilizationCommunity = aiUtilizationCommunity;
        this.avgPresentationCommunity = presentationCommunity;
        this.finalTotalScore = totalScore;
    }
}
