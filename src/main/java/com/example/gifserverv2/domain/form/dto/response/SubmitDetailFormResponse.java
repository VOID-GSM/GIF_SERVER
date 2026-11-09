package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.FormSubmit;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "제출 내역")
public record SubmitDetailFormResponse(
        @Schema(description = "제출 ID", example = "8")
        Long submitId,
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "마지막으로 제출·수정한 사용자 ID", example = "12")
        Long submittedByUserId,
        @Schema(description = "마지막으로 제출·수정한 사용자 이름", example = "홍길동")
        String submittedByName,
        @Schema(description = "마지막으로 제출·수정한 사용자 학번", example = "2101")
        String submittedByStudentNumber,
        @Schema(description = "최초 제출 일시", example = "2026-11-20T15:30:00")
        LocalDateTime submittedAt,
        @Schema(description = "마감 준수 여부. 선생님이 지정했으면 그 값, 아니면 제출 시각 기준", example = "true")
        boolean deadlineComplied,
        @Schema(description = "답변 목록")
        List<AnswerResponse> answers
) {
    @Schema(description = "항목별 답변")
    public record AnswerResponse(
            @Schema(description = "항목 ID", example = "21")
            Long fieldId,
            @Schema(description = "항목 제목", example = "프로젝트 진행 상황")
            String fieldTitle,
            @Schema(description = "항목 유형. TEXT(텍스트) / FILE(파일) / CALENDAR(일정)", example = "TEXT")
            String type,
            @Schema(description = "TEXT 답변", example = "기획을 마치고 개발을 시작했습니다.")
            String textAnswer,
            @Schema(description = "FILE 답변 파일 URL")
            String filePath,
            @Schema(description = "파일 크기 (byte)")
            Long fileSize,
            @Schema(description = "원본 파일명")
            String originalFileName,
            @Schema(description = "CALENDAR 답변")
            List<CalendarEventResponse> dateAnswer
    ) {}

    @Schema(description = "일정 답변")
    public record CalendarEventResponse(
            @Schema(description = "일정 이름", example = "기획")
            String eventName,
            @Schema(description = "시작일", example = "2026-11-01")
            LocalDate startDate,
            @Schema(description = "종료일", example = "2026-11-07")
            LocalDate endDate,
            @Schema(description = "표시 색상", example = "#FF8A00")
            String color
    ) {}

    public static SubmitDetailFormResponse from(FormSubmit submit, String teamName, String submittedByName, String submittedByStudentNumber) {
        boolean deadlineComplied = submit.isDeadlineComplied();

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