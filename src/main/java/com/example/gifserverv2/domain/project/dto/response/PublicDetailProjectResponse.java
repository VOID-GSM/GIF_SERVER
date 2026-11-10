package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

// 비로그인/외부 사용자용 응답. 팀원의 학번, userId는 담지 않는다.
@Schema(description = "프로젝트 상세 (비로그인·외부 사용자용). 팀원의 userId·학번은 포함하지 않는다")
public record PublicDetailProjectResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long id,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String name,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "프로젝트 설명. `lang=ja`이고 일본어 번역이 있으면 일본어, 없으면 한국어 원문", example = "아이디어 페스티벌 운영 플랫폼")
        String description,
        @Schema(description = "description 이 AI 번역문인지. 한국어 원문이면 false", example = "false")
        boolean descriptionTranslated,
        @Schema(description = "로고 이미지 URL. 없으면 null", example = "/files/project-logos/abc.png")
        String logo,
        @Schema(description = "학년", example = "2")
        Integer grade,
        @Schema(description = "팀원 목록")
        List<PublicMemberInfo> members,
        @Schema(description = "좋아요 수", example = "12")
        long likeCount,
        @Schema(description = "내가 좋아요를 눌렀는지. 비로그인이면 false", example = "false")
        @JsonProperty("isLiked") boolean isLiked,
        @Schema(description = "조회수", example = "340")
        long viewCount,
        @Schema(description = "댓글 수 (숨김·삭제 제외)", example = "5")
        long commentCount
) implements ProjectDetailResponse {

    @Schema(description = "팀원 정보 (공개용)")
    public record PublicMemberInfo(
            @Schema(description = "이름", example = "홍길동")
            String name,
            @Schema(description = "팀 내 역할. LEADER / MEMBER", example = "MEMBER")
            String role
    ) {}

    public static PublicDetailProjectResponse from(Project project, LocalizedText description, Map<Long, UserEntity> userMap,
                                                   long likeCount, boolean isLiked,
                                                   long viewCount, long commentCount) {
        List<PublicMemberInfo> members = project.getMembers().stream()
                .map(m -> {
                    UserEntity user = userMap.get(m.getUserId());
                    return new PublicMemberInfo(user != null ? user.getName() : null, m.getRole().name());
                })
                .toList();

        return new PublicDetailProjectResponse(
                project.getId(), project.getName(), project.getTeamName(),
                description.text(), description.translated(), project.getLogo(), project.getGrade(),
                members, likeCount, isLiked, viewCount, commentCount
        );
    }
}