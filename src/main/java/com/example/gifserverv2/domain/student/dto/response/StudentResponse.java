package com.example.gifserverv2.domain.student.dto.response;

import com.example.gifserverv2.domain.user.entity.ClientRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "학생 정보")
public record StudentResponse(
        @Schema(description = "학생 userId", example = "12")
        Long userId,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "학번", example = "2101")
        String studentNumber,
        @Schema(description = "학년", example = "2")
        String grade,
        @Schema(description = "이메일", example = "s24001@gsm.hs.kr")
        String email,
        @Schema(description = "소속 팀 목록. 미배정이면 빈 배열")
        List<TeamInfo> teams,
        @Schema(description = "대기 중인 팀 배정 요청. 없으면 null")
        PendingRequestInfo pendingRequest
) {
    @Schema(description = "소속 팀")
    public record TeamInfo(
            @Schema(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Schema(description = "프로젝트 이름", example = "GIF")
            String projectName,
            @Schema(description = "팀 이름", example = "VOID")
            String teamName,
            @Schema(description = "팀 내 역할", example = "MEMBER")
            ClientRole role
    ) {
    }

    @Schema(description = "대기 중인 팀 배정 요청")
    public record PendingRequestInfo(
            @Schema(description = "팀원 추가 요청 ID", example = "9")
            Long requestId,
            @Schema(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Schema(description = "프로젝트 이름", example = "GIF")
            String projectName,
            @Schema(description = "팀 이름", example = "VOID")
            String teamName
    ) {
    }
}
