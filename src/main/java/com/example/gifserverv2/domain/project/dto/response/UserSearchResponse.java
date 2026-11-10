package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "팀원 후보 검색 결과")
public record UserSearchResponse(
        @Schema(description = "사용자 ID", example = "12")
        Long userId,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "학번", example = "2101")
        String studentNumber,
        @Schema(description = "이미 다른 팀에 소속되어 있는지. true면 팀원으로 추가할 수 없다", example = "false")
        boolean hasTeam
) {
    public static UserSearchResponse from(UserEntity user, boolean hasTeam) {
        return new UserSearchResponse(
                user.getId(),
                user.getName(),
                user.getStudentNumber(),
                hasTeam
        );
    }
}