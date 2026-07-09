package com.example.gifserverv2.domain.score.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchSocialScoreRequest {
    private Integer userExperience;
    private Integer socialValueCommunity;
    private Integer aiUtilizationCommunity;
    private Integer presentationCommunity;
}
