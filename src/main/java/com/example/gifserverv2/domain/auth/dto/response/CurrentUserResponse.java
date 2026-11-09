package com.example.gifserverv2.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 정보")
public record CurrentUserResponse(
        @Schema(description = "사용자 ID", example = "12")
        Long id,
        @Schema(description = "이메일", example = "s24001@gsm.hs.kr")
        String email,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "학번. 학생이 아니면 null", example = "2101")
        String studentNumber,
        @Schema(description = "학년. 학생이 아니면 null", example = "2")
        String grade,
        @Schema(description = "권한. USER / ADMIN / EXTERNAL", example = "USER")
        String role,
        @Schema(description = "계정 유형. STUDENT / TEACHER / EXTERNAL", example = "STUDENT")
        String accountType,
        @Schema(description = "선생님 역할. 선생님이 아니면 null")
        String adminRole,
        @Schema(description = "선생님 담당 팀. 없으면 null")
        String adminTeam,
        @Schema(description = "학년부장 여부", example = "false")
        boolean isGradeHead,
        @Schema(description = "학생 팀 내 역할 LEADER / MEMBER. 없으면 null", example = "LEADER")
        String clientRole,
        @Schema(description = "소속 프로젝트 ID (팀장인 프로젝트 우선). 없으면 null", example = "1")
        Long projectId,
        @Schema(description = "소속 팀 이름. 없으면 null", example = "VOID")
        String clientTeam,
        @Schema(description = "연동된 GitHub 아이디. 없으면 null", example = "octocat")
        String githubUsername,
        @Schema(description = "GitHub 아바타 URL. 없으면 null")
        String githubAvatarUrl,
        @Schema(description = "코인 잔액", example = "120")
        int coinBalance,
        @Schema(description = "새로 발급된 토큰. 내 정보 수정 응답에서만 채워지고 그 외에는 null")
        String accessToken
) {
}