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
        String submittedByName,
        String submittedByStudentNumber,
        LocalDateTime submittedAt,
        boolean deadlineComplied,
        List<AnswerResponse> answers
) {
    public record AnswerResponse(
            Long fieldId,
            String fieldTitle,
            String type,
            String textAnswer,
            String filePath,
            Long fileSize,
            String originalFileName,
            List<CalendarEventResponse> dateAnswer
    ) {}

    public record CalendarEventResponse(
            String eventName,
            LocalDate startDate,
            LocalDate endDate,
            String color
    ) {}

    public static SubmitDetailFormResponse from(FormSubmit submit, String teamName, String submittedByName, String submittedByStudentNumber) {
        boolean deadlineComplied = !submit.getSubmittedAt().isAfter(submit.getForm().getDeadline());

        List<AnswerResponse> answerResponses = submit.getAnswers().stream()
                .map(a -> {
                    List<CalendarEventResponse> events = a.getCalendarEvents().stream()
                            .map(e -> new CalendarEventResponse(
                                    e.getEventName(),
                                    e.getStartDate(),
                                    e.getEndDate(),
                                    e.getColor()
                            ))
                            .toList();

                    return new AnswerResponse(
                            a.getFormField().getId(),
                            a.getFormField().getTitle(),
                            a.getFormField().getType().name(),
                            a.getTextAnswer(),
                            a.getFilePath(),
                            a.getFileSize(),
                            a.getOriginalFileName(),
                            events
                    );
                })
                .toList();

        return new SubmitDetailFormResponse(
                submit.getId(),
                submit.getProjectId(),
                teamName,
                submit.getSubmittedByUserId(),
                submittedByName,
                submittedByStudentNumber,
                submit.getSubmittedAt(),
                deadlineComplied,
                answerResponses
        );
    }
}