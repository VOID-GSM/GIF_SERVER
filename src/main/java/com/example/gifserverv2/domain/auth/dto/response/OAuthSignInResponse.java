package com.example.gifserverv2.domain.auth.dto.response;

public record OAuthSignInResponse(
        String accessToken,
        Long userId,
        String email,
        String name,
        String studentNumber,
        String grade,
        String role,
        String accountType,
        String adminRole,
        String adminTeam,
        boolean gradeHead,
        String clientRole,
        String returnTo) {

    /**
     * 로그인 시작 시 받은 복귀 경로를 담은 응답을 만든다.
     */
    public OAuthSignInResponse withReturnTo(String returnTo) {
        return new OAuthSignInResponse(accessToken, userId, email, name, studentNumber, grade, role,
                accountType, adminRole, adminTeam, gradeHead, clientRole, returnTo);
    }
}
