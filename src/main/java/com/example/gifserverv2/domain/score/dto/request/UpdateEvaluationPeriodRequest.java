package com.example.gifserverv2.domain.score.dto.request;

import com.example.gifserverv2.domain.score.entity.ScoreCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class UpdateEvaluationPeriodRequest {

    @NotNull(message = "평가 카테고리는 필수 항목입니다.")
    private ScoreCategory category;

    @NotNull(message = "평가 시작일은 필수 항목입니다.")
    private LocalDateTime startDate;
}