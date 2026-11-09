package com.example.gifserverv2.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record OAuthSignInResponse(
        @Schema(description = "API 호출에 사용할 JWT. Authorization: Bearer 헤더로 보낸다", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "이메일", example = "s24001@gsm.hs.kr")
        String email,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "학번. 학생이 아니면 null", example = "2101")
        String studentNumber,
        @Schema(description = "학년. 학생이 아니면 null", example = "2")
        String grade,
        @Schema(description = "권한. USER(학생) / ADMIN(선생님) / EXTERNAL(외부 사용자)", example = "USER")
        String role,
        @Schema(description = "계정 유형. STUDENT / TEACHER / EXTERNAL", example = "STUDENT")
        String accountType,
        @Schema(description = "선생님 역할. MASTER / MAJOR_TEACHER / GENERAL_TEACHER / VOID. 선생님이 아니거나 추가 정보 미입력이면 null", example = "MAJOR_TEACHER")
        String adminRole,
        @Schema(description = "선생님 담당 팀. 없으면 null")
        String adminTeam,
        @Schema(description = "학년부장 여부", example = "false")
        boolean gradeHead,
        @Schema(description = "학생 팀 내 역할. LEADER / MEMBER. 학생이 아니거나 추가 정보 미입력이면 null", example = "LEADER")
        String clientRole,
        @Schema(description = "로그인 시작 시 넘긴 복귀 경로. 없거나 허용되지 않은 경로면 null", example = "/project/1")
        String returnTo) {

    /**
     * 로그인 시작 시 받은 복귀 경로를 담은 응답을 만든다.
     */
    public OAuthSignInResponse withReturnTo(String returnTo) {
        return new OAuthSignInResponse(accessToken, userId, email, name, studentNumber, grade, role,
                accountType, adminRole, adminTeam, gradeHead, clientRole, returnTo);
    }
}
