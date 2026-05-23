package com.example.gifserverv2.domain.score.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReportScoreRequest {
    private Long projectId;
    private String evaluatorId;
    private Integer reportWriting;
    private Integer reportContent;
    private Integer aiUsagePlan;
    private Integer creativity;
}
