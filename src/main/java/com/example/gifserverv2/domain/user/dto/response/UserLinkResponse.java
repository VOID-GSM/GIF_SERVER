package com.example.gifserverv2.domain.user.dto.response;

import com.example.gifserverv2.domain.user.entity.UserLink;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인 링크")
public record UserLinkResponse(
        @Schema(description = "링크 ID", example = "2")
        Long id,
        @Schema(description = "표시 이름", example = "블로그")
        String title,
        @Schema(description = "주소", example = "https://velog.io/@octocat")
        String url
) {

    public static UserLinkResponse from(UserLink link) {
        return new UserLinkResponse(link.getId(), link.getTitle(), link.getUrl());
    }
}
