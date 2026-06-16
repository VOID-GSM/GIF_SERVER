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
            LocalDate dateAnswer,
            String eventName,
            LocalDate startDate,
            LocalDate endDate,
            String color
    ) {}
}