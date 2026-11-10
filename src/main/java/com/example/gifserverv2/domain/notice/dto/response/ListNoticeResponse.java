package com.example.gifserverv2.domain.notice.dto.response;

import com.example.gifserverv2.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지 목록 항목")
public record ListNoticeResponse(
        @Schema(description = "공지 ID", example = "5")
        Long id,
        @Schema(description = "제목", example = "중간 발표 일정 안내")
        String title,
        @Schema(description = "대상 학년", example = "[1, 2]")
        List<Integer> targetGrades,
        @Schema(description = "대상 팀 이름", example = "[\"VOID\"]")
        List<String> targetTeamNames,
        @Schema(description = "작성 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
    public static ListNoticeResponse from(Notice notice, List<String> targetTeamNames) {
        return new ListNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getTargetGrades(),
                targetTeamNames,
                notice.getCreatedAt()
        );
    }
}