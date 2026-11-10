package com.example.gifserverv2.domain.form.dto.response;

import com.example.gifserverv2.domain.form.entity.Form;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "양식 목록 항목")
public record ListFormResponse(
        @Schema(description = "양식 ID", example = "3")
        Long id,
        @Schema(description = "양식 제목", example = "1차 중간 보고서")
        String title,
        @Schema(description = "마감 일시", example = "2026-11-30T23:59:59")
        LocalDateTime deadline,
        @Schema(description = "공지 여부", example = "true")
        boolean announced,
        @Schema(description = "projectId로 조회한 팀의 제출 여부. projectId 없이 조회하면 false", example = "false")
        boolean submitted,
        @Schema(description = "마감 준수 여부. 미제출이거나 projectId 없이 조회하면 null")
        Boolean deadlineComplied,
        @Schema(description = "대상 학년. null이면 전체", example = "2")
        Integer targetGrade
) {
    public static ListFormResponse from(Form form) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), false, null, form.getTargetGrade());
    }

    public static ListFormResponse from(Form form, boolean submitted, Boolean deadlineComplied) {
        return new ListFormResponse(form.getId(), form.getTitle(), form.getDeadline(), form.isAnnounced(), submitted, deadlineComplied, form.getTargetGrade());
    }
}