package com.example.gifserverv2.domain.engagement.dto.response;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.user.entity.AccountType;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "댓글")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "10")
        Long id,
        @Schema(description = "작성자")
        Author author,
        @Schema(description = "댓글 내용", example = "멋진 프로젝트네요!")
        String content,
        @Schema(description = "작성 시각 (UTC, ISO-8601)", example = "2026-11-09T01:23:45Z")
        Instant createdAt,
        @Schema(description = "내가 쓴 댓글인지. 비로그인이면 false", example = "false")
        @JsonProperty("isMine") boolean isMine
) {

    @Schema(description = "댓글 작성자")
    public record Author(
            @Schema(description = "작성자 userId", example = "12")
            Long id,
            @Schema(description = "작성자 이름", example = "홍길동")
            String name,
            @Schema(description = "프로필 이미지 URL. 없으면 null")
            String profileImage,
            @Schema(description = "계정 유형. STUDENT / TEACHER / EXTERNAL. TEACHER는 프로필 조회가 404라 링크를 비활성화한다. 사용자를 찾을 수 없으면 null", example = "STUDENT", nullable = true)
            AccountType accountType
    ) {}

    /** @param author 작성자. 찾을 수 없으면 null */
    public static CommentResponse from(ProjectComment c, UserEntity author, boolean isMine) {
        return new CommentResponse(
                c.getId(),
                new Author(c.getUserId(), c.getAuthorName(),
                        author != null ? author.getDisplayProfileImageUrl() : null,
                        author != null ? AccountType.of(author) : null),
                c.getContent(),
                c.getCreatedAt(),
                isMine);
    }
}