package com.example.gifserverv2.domain.form.dto.request;

import java.time.LocalDate;

public record UpdateSubmitRequest(
        Long submitId,
        List<UpdateSubmitAnswerRequest> answers
) {
    public record CalendarEventRequest(
            String eventName,
            LocalDate startDate,
            LocalDate endDate,
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