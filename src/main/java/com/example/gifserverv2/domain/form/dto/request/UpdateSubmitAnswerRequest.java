package com.example.gifserverv2.domain.form.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "양식 수정 답변 요청")
public record UpdateSubmitAnswerRequest(
        @Schema(description = "항목 ID", example = "21")
        Long fieldId,
        @Schema(description = "TEXT 항목 답변 (최대 10000자)", example = "기획을 마치고 개발을 시작했습니다.")
        String textAnswer,
        @Schema(description = "CALENDAR 항목 답변 (일정 목록)")
        List<UpdateSubmitRequest.CalendarEventRequest> dateAnswer,
        @Schema(description = "FILE 항목 답변. 업로드 API가 돌려준 파일 경로")
        String filePath,
        @Schema(description = "파일 크기 (byte)")
        Long fileSize,
        @Schema(description = "원본 파일명", example = "보고서.pdf")
        String originalFileName
) {}
