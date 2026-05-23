package com.example.gifserverv2.domain.score.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateMajorScoreRequest {
    private Long projectId;
    private String evaluatorId;
    private Integer technicalCompleteness;
    private Integer socialValueMajor;
    private Integer aiUtilityMajorScore;
    private Integer presentationMajor;
}
