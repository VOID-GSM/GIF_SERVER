package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// 비로그인/외부 사용자용 응답. 팀원의 학번, userId는 담지 않는다.
public record PublicDetailProjectResponse(
        Long id,
        String name,
        String teamName,
        String description,
        String logo,
        Integer grade,
        List<PublicMemberInfo> members,
        long likeCount,
        @JsonProperty("isLiked") boolean isLiked,
        long viewCount,
        long commentCount
) implements ProjectDetailResponse {

    public record PublicMemberInfo(String name, String role) {}

    public static PublicDetailProjectResponse from(Project project, Map<Long, UserEntity> userMap,
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
                project.getDescription(), project.getLogo(), project.getGrade(),
                members, likeCount, isLiked, viewCount, commentCount
        );
    }
}