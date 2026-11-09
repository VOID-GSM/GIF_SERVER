package com.example.gifserverv2.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "마이페이지 정보")
public record MypageResponse(
        @Schema(description = "사용자 ID", example = "12")
        Long id,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "이메일", example = "s24001@gsm.hs.kr")
        String email,
        @Schema(description = "프로필 이미지 URL. 업로드한 이미지 > GitHub 아바타 > null 순")
        String profileImageUrl,
        @Schema(description = "직접 업로드한 이미지인지 (삭제 버튼 노출용)", example = "false")
        boolean customProfileImage,
        @Schema(description = "학번", example = "2101")
        String studentNumber,
        @Schema(description = "학년 (학번 첫째 자리)", example = "2")
        Integer grade,
        @Schema(description = "반 (학번 둘째 자리)", example = "1")
        Integer classNo,
        @Schema(description = "전공", example = "백엔드")
        String major,
        @Schema(description = "기술 스택", example = "[\"Spring\", \"React\"]")
        List<String> techStacks,
        @Schema(description = "소속 팀 목록 (팀장인 팀이 먼저)")
        List<TeamInfo> teams,
        @Schema(description = "개인 링크 목록")
        List<UserLinkResponse> links
) {

    /**
     * @param role 팀 내 역할 (LEADER / MEMBER)
     * @param part 팀 내 파트 (예: 백엔드)
     */
    @Schema(description = "소속 팀")
    public record TeamInfo(
            @Schema(description = "프로젝트 ID", example = "1")
            Long projectId,
            @Schema(description = "프로젝트 이름", example = "GIF")
            String projectName,
            @Schema(description = "팀 이름", example = "VOID")
            String teamName,
            @Schema(description = "팀 내 역할 LEADER / MEMBER", example = "LEADER")
            String role,
            @Schema(description = "팀 내 파트", example = "백엔드")
            String part
    ) {
    }
}
