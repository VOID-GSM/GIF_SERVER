package com.example.gifserverv2.domain.score.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateScoreRequest {
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
}
