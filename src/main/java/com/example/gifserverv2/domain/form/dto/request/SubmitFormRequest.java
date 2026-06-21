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

                if (color != null && color.length() > 7) {
                    throw new IllegalArgumentException("색상 코드는 7자 이하이어야 합니다.");
                }
            }
        }
    }
}