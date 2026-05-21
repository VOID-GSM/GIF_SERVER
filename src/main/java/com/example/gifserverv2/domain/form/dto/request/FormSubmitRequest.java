package com.example.gifserverv2.domain.form.dto.request;

import java.time.LocalDate;
import java.util.List;

public record FormSubmitRequest(
        Long formId,
        Long projectId,
        List<AnswerRequest> answers
) {
    public record AnswerRequest(
            Long fieldId,
            String textAnswer,
            LocalDate dateAnswer
    ) {}
}