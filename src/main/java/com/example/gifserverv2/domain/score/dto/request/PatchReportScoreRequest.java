package com.example.gifserverv2.domain.score.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchReportScoreRequest {
    private Integer reportWriting;
    private Integer reportContent;
    private Integer aiUsagePlan;
    private Integer creativity;
}
