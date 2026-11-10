package com.example.gifserverv2.domain.student.dto.response;

import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "팀원 추가(팀 배정) 요청")
public record TeamJoinRequestResponse(
        @Schema(description = "팀원 추가 요청 ID", example = "9")
        Long requestId,
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String projectName,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "학생 userId", example = "12")
        Long userId,
        @Schema(description = "학생 이름", example = "홍길동")
        String studentName,
        @Schema(description = "학번", example = "2101")
        String studentNumber,
        @Schema(description = "상태. PENDING(대기) / ACCEPTED(수락) / REJECTED(거절) / CANCELLED(취소)", example = "PENDING")
        Status status,
        @Schema(description = "거절 사유. 없으면 null")
        String rejectReason,
        @Schema(description = "요청 일시", example = "2026-11-09T10:00:00")
        LocalDateTime createdAt
) {
}
