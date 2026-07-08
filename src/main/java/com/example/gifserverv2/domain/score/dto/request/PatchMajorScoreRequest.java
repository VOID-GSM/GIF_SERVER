package com.example.gifserverv2.domain.score.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchMajorScoreRequest {
    private Integer technicalCompleteness;
    private Integer socialValueMajor;
    private Integer aiUtilityMajorScore;
    private Integer presentationMajor;
}
