package com.example.gifserverv2.domain.form.dto.request;

import java.time.LocalDate;
import java.util.List;

public record SubmitFormRequest(
        Long formId,
        Long projectId,
        List<AnswerRequest> answers
) {
    public record AnswerRequest(
            Long fieldId,
            String textAnswer,
            List<CalendarEventRequest> dateAnswer,
            String filePath,
            Long fileSize
    ) {}

    public record CalendarEventRequest(
            String eventName,
            String startDate,
            String endDate,
            String color
    ) {
        public CalendarEventRequest {
            if (startDate != null && endDate != null
                    && java.time.LocalDate.parse(startDate).isAfter(java.time.LocalDate.parse(endDate))) {
                throw new IllegalArgumentException("시작일은 종료일보다 이전이어야 합니다.");
            }
            if (color != null && color.length() > 7) {
                throw new IllegalArgumentException("색상 코드는 7자 이하이어야 합니다.");
            }
        }
    }
}