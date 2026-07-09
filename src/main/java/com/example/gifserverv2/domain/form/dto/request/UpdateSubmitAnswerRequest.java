package com.example.gifserverv2.domain.form.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "양식 수정 답변 요청")
public record UpdateSubmitAnswerRequest(
        Long fieldId,
        String textAnswer,
        List<UpdateSubmitRequest.CalendarEventRequest> dateAnswer,
        String filePath,
        Long fileSize,
        String originalFileName
) {}
