package com.example.gifserverv2.domain.form.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "양식 제출 요청")
public record SubmitFormRequest(
        @Schema(description = "양식 ID", example = "3")
        Long formId,
        @Schema(description = "제출하는 프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "항목별 답변 목록")
        List<AnswerRequest> answers
) {
    @Schema(description = "항목별 답변. 항목 유형에 맞는 필드만 채운다")
    public record AnswerRequest(
            @Schema(description = "항목 ID", example = "21")
            Long fieldId,
            @Schema(description = "TEXT 항목 답변 (최대 10000자)", example = "기획을 마치고 개발을 시작했습니다.")
            String textAnswer,
            @Schema(description = "CALENDAR 항목 답변 (일정 목록)")
            List<CalendarEventRequest> dateAnswer,
            @Schema(description = "FILE 항목 답변. 업로드 API가 돌려준 파일 경로")
            String filePath,
            @Schema(description = "파일 크기 (byte)")
            Long fileSize
    ) {}

    @Schema(description = "일정 답변")
    public record CalendarEventRequest(
            @Schema(description = "일정 이름", example = "기획")
            String eventName,
            @Schema(description = "시작일 (yyyy-MM-dd)", example = "2026-11-01")
            String startDate,
            @Schema(description = "종료일 (yyyy-MM-dd). 시작일 이후여야 한다", example = "2026-11-07")
            String endDate,
            @Schema(description = "표시 색상 (최대 7자)", example = "#FF8A00")
            String color
    ) {
        public CalendarEventRequest {
            if (startDate != null && endDate != null) {
                try {
                    java.time.LocalDate start = java.time.LocalDate.parse(startDate);
                    java.time.LocalDate end = java.time.LocalDate.parse(endDate);
                    if (start.isAfter(end)) {
                        throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException("올바르지 않은 날짜 형식입니다. yyyy-MM-dd 형식이어야 합니다.");
                }
            }
            if (color != null && color.length() > 7) {
                throw new IllegalArgumentException("색상 코드는 7자 이하이어야 합니다.");
            }
        }
    }
}