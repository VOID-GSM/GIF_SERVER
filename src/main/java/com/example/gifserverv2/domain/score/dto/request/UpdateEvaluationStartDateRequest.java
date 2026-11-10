package com.example.gifserverv2.domain.score.dto.request;

import com.example.gifserverv2.domain.score.entity.ScoreCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "평가 시작일 설정 요청")
@Getter
@NoArgsConstructor
public class UpdateEvaluationStartDateRequest {

    @Schema(description = "평가 영역. MAJOR(전공) / REPORT(보고서) / SOCIAL(사회)", example = "MAJOR")
    @NotNull(message = "평가 카테고리는 필수 항목입니다.")
    private ScoreCategory category;

    @Schema(description = "평가 시작 일시. 올해 12월 29일 23:59:59 이전", example = "2026-12-20T09:00:00")
    @NotNull(message = "평가 시작일은 필수 항목입니다.")
    private LocalDateTime startDate;
}