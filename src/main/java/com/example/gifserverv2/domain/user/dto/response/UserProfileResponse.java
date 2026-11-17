package com.example.gifserverv2.domain.user.dto.response;

import com.example.gifserverv2.domain.user.entity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 다른 사용자에게 보여 주는 공개 프로필. 마이페이지 응답에서 이메일과 업로드 이미지 여부를 빼고 계정 유형을 더한 것이다.
 */
@Schema(description = "공개 프로필")
public record UserProfileResponse(
        @Schema(description = "사용자 ID", example = "12")
        Long id,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "프로필 이미지 URL. 업로드한 이미지 > GitHub 아바타 > null 순")
        String profileImageUrl,
        @Schema(description = "계정 유형. STUDENT(학교) / EXTERNAL(외부)", example = "STUDENT",
                allowableValues = {"STUDENT", "EXTERNAL"})
        AccountType accountType,
        @Schema(description = "학번. 외부 사용자는 null", example = "2101")
        String studentNumber,
        @Schema(description = "학년 (학번 첫째 자리). 외부 사용자는 null", example = "2")
        Integer grade,
        @Schema(description = "반 (학번 둘째 자리). 외부 사용자는 null", example = "1")
        Integer classNo,
        @Schema(description = "전공", example = "백엔드")
        String major,
        @Schema(description = "기술 스택", example = "[\"Spring\", \"Kotlin\"]")
        List<String> techStacks,
        @Schema(description = "소속 팀 목록 (팀장인 팀이 먼저). 외부 사용자는 빈 배열")
        List<MypageResponse.TeamInfo> teams,
        @Schema(description = "개인 링크 목록")
        List<UserLinkResponse> links
) {

    public static UserProfileResponse from(MypageResponse mypage, AccountType accountType) {
        return new UserProfileResponse(
                mypage.id(),
                mypage.name(),
                mypage.profileImageUrl(),
                accountType,
                mypage.studentNumber(),
                mypage.grade(),
                mypage.classNo(),
                mypage.major(),
                mypage.techStacks(),
                mypage.teams(),
                mypage.links()
        );
    }
}
