package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "양식 상세")
public record DetailFormResponse(
        @Schema(description = "양식 ID", example = "3")
        Long id,
        @Schema(description = "양식 제목", example = "1차 중간 보고서")
        String title,
        @Schema(description = "양식 설명", example = "11월 30일까지 제출")
        String description,
        @Schema(description = "마감 일시", example = "2026-11-30T23:59:59")
        LocalDateTime deadline,
        @Schema(description = "공지 여부", example = "true")
        boolean announced,
        @Schema(description = "projectId로 조회한 팀의 마감 준수 여부. projectId가 없거나 미제출이면 null", example = "true")
        Boolean deadlineComplied,
        @Schema(description = "항목 목록")
        List<FieldResponse> fields
) {
    @Schema(description = "양식 항목")
    public record FieldResponse(
            @Schema(description = "항목 ID", example = "21")
            Long id,
            @Schema(description = "항목 제목", example = "프로젝트 진행 상황")
            String title,
            @Schema(description = "항목 설명", example = "이번 주까지 진행한 내용을 적어 주세요.")
            String description,
            @Schema(description = "항목 유형. TEXT(텍스트) / FILE(파일) / CALENDAR(일정)", example = "TEXT")
            String type,
            @Schema(description = "표시 순서", example = "0")
            int orderIndex,
            @Schema(description = "FILE 항목 허용 확장자. 비어 있으면 서버가 허용하는 전체 확장자", example = "[\"pdf\"]")
            List<String> allowedExtensions,
            @Schema(description = "필수 항목 여부", example = "true")
            boolean required
    ) {}

    public static DetailFormResponse from(Form form, Boolean deadlineComplied) {
        List<FieldResponse> fieldResponses = form.getFields().stream()
                .map(f -> new FieldResponse(
                        f.getId(),
                        f.getTitle(),
                        f.getDescription(),
                        f.getType().name(),
                        f.getOrderIndex(),
                        f.getAllowedExtensionList(),
                        f.isRequired()
                ))
                .toList();

        return new DetailFormResponse(
                form.getId(),
                form.getTitle(),
                form.getDescription(),
                form.getDeadline(),
                form.isAnnounced(),
                deadlineComplied,
                fieldResponses
        );
    }
}