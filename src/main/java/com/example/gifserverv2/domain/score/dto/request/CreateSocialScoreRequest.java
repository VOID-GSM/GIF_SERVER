package com.example.gifserverv2.domain.score.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateSocialScoreRequest {
    private Long projectId;
    private String evaluatorId;
    private Integer userExperience;
    private Integer socialValueCommunity;
    private Integer aiUtilizationCommunity;
    private Integer presentationCommunity;
}
