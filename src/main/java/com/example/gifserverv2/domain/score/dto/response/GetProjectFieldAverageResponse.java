package com.example.gifserverv2.domain.score.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetProjectFieldAverageResponse {
    private Long projectId;
    private int majorAverage;
    private int reportAverage;
    private int communityAverage;
    private int grandTotalAverage;
}