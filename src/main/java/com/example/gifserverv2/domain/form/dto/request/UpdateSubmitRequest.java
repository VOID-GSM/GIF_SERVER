package com.example.gifserverv2.domain.form.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "제출 수정 요청. 답변 전체를 교체한다")
public record UpdateSubmitRequest(
        @Schema(description = "제출 ID", example = "8")
        Long submitId,
        @Schema(description = "항목별 답변 목록 (필수). 없는 항목의 기존 답변은 삭제된다")
        List<UpdateSubmitAnswerRequest> answers
) {
    @Schema(description = "일정 답변")
    public record CalendarEventRequest(
            @Schema(description = "일정 이름", example = "기획")
            String eventName,
            @Schema(description = "시작일", example = "2026-11-01")
            LocalDate startDate,
            @Schema(description = "종료일. 시작일 이후여야 한다", example = "2026-11-07")
            LocalDate endDate,
            @Schema(description = "표시 색상 (최대 7자)", example = "#FF8A00")
            String color
    ) {
        public CalendarEventRequest {
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
            }
            if (color != null && color.length() > 7) {
                throw new IllegalArgumentException("색상 코드는 7자 이하이어야 합니다.");
            }
        }
    }
}