package com.example.gifserverv2.domain.score.dto.request;

import com.example.gifserverv2.domain.score.entity.ScoreCategory;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UpdateEvaluationPeriodRequest {
    private ScoreCategory category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}