package com.example.gifserverv2.domain.notice.dto.response;

import com.example.gifserverv2.domain.notice.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "공지 상세")
public record DetailNoticeResponse(
        @Schema(description = "공지 ID", example = "5")
        Long id,
        @Schema(description = "제목", example = "중간 발표 일정 안내")
        String title,
        @Schema(description = "내용", example = "11월 20일 오후 2시에 진행합니다.")
        String content,
        @Schema(description = "대상 학년", example = "[1, 2]")
        List<Integer> targetGrades,
        @Schema(description = "대상 팀 이름 (삭제된 프로젝트는 제외)", example = "[\"VOID\"]")
        List<String> targetTeamNames,
        @Schema(description = "작성 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
    public static DetailNoticeResponse from(Notice notice, List<String> targetTeamNames) {
        return new DetailNoticeResponse(
                notice.getId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getTargetGrades(),
                targetTeamNames,
                notice.getCreatedAt()
        );
    }
}