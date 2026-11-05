package com.example.gifserverv2.domain.user.dto.response;

import java.util.List;

/**
 * 마이페이지 기본 정보.
 *
 * @param profileImageUrl       업로드한 이미지, 없으면 GitHub 아바타, 둘 다 없으면 null
 * @param customProfileImage    직접 업로드한 이미지인지 여부 (삭제 버튼 노출용)
 * @param grade                 학년 (학번 첫째 자리)
 * @param classNo               반 (학번 둘째 자리)
 * @param teams                 소속 팀 목록 (팀장인 팀이 먼저)
 */
public record MypageResponse(
        Long id,
        String name,
        String email,
        String profileImageUrl,
        boolean customProfileImage,
        String studentNumber,
        Integer grade,
        Integer classNo,
        String major,
        List<String> techStacks,
        List<TeamInfo> teams,
        List<UserLinkResponse> links
) {

    /**
     * @param role 팀 내 역할 (LEADER / MEMBER)
     * @param part 팀 내 파트 (예: 백엔드)
     */
    public record TeamInfo(Long projectId, String projectName, String teamName, String role, String part) {
    }
}
