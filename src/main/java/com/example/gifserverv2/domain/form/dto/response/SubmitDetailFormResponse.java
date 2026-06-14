package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.FormSubmit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SubmitDetailFormResponse(
        Long submitId,
        Long projectId,
        String teamName,
        Long submittedByUserId,
        LocalDateTime submittedAt,
        List<AnswerResponse> answers
) {
    public record AnswerResponse(
            Long fieldId,
            String fieldTitle,
            String type,
            String textAnswer,
            String filePath,
            LocalDate dateAnswer
    ) {}

    public static SubmitDetailFormResponse from(FormSubmit submit, String teamName) {
        List<AnswerResponse> answerResponses = submit.getAnswers().stream()
                .map(a -> new AnswerResponse(
                        a.getFormField().getId(),
                        a.getFormField().getTitle(),
                        a.getFormField().getType().name(),
                        a.getTextAnswer(),
                        a.getFilePath(),
                        a.getDateAnswer()
                ))
                .toList();

        return new SubmitDetailFormResponse(
                submit.getId(),
                submit.getProjectId(),
                teamName,
                submit.getSubmittedByUserId(),
                submit.getSubmittedAt(),
                answerResponses
        );
    }
}