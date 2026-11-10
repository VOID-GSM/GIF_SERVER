package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectTeacherAssignment.AssignmentStatus;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "선생님 배정·채점 현황")
public record TeacherListResponse(
        @Schema(description = "선생님 userId", example = "7")
        Long id,
        @Schema(description = "이메일", example = "teacher@gsm.hs.kr")
        String email,
        @Schema(description = "이름", example = "김선생")
        String name,
        @Schema(description = "선생님 역할", example = "MAJOR_TEACHER")
        AdminRole adminRole,
        @Schema(description = "담당 팀 이름. 없으면 null", example = "VOID")
        String adminTeam,
        @Schema(description = "학년부장 여부", example = "false")
        boolean isGradeHead,
        @Schema(description = "최근 담당 교사 배정 요청. 없으면 null")
        AssignmentInfo assignmentInfo,
        @Schema(description = "모든 프로젝트 채점 완료 여부", example = "false")
        boolean isScoreSubmitted,
        @Schema(description = "아직 채점하지 않은 프로젝트 목록")
        List<UnsubmittedProjectInfo> unsubmittedProjects
) {
    @Schema(description = "가장 최근 담당 교사 배정 요청")
    public record AssignmentInfo(
            @Schema(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Schema(description = "상태. PENDING / ACCEPTED / REJECTED", example = "PENDING")
            AssignmentStatus status,
            @Schema(description = "거절 사유. 거절이 아니면 null")
            String rejectReason
    ) {}

    @Schema(description = "아직 채점하지 않은 프로젝트")
    public record UnsubmittedProjectInfo(
            @Schema(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Schema(description = "프로젝트 이름", example = "GIF")
            String projectName,
            @Schema(description = "팀 이름", example = "VOID")
            String teamName
    ) {}
}