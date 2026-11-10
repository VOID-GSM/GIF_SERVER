package com.example.gifserverv2.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 개인 사이트 링크 추가/수정.
 *
 * @param title 표시 이름 (예: "블로그", 선택)
 * @param url   http/https 주소
 */
@Schema(description = "개인 링크 추가·수정 요청")
public record UserLinkRequest(
        @Schema(description = "표시 이름 (최대 30자, 선택)", example = "블로그")
        @Size(max = 30, message = "링크 이름은 30자 이하여야 합니다.")
        String title,

        @Schema(description = "http/https 주소 (최대 500자)", example = "https://velog.io/@octocat")
        @NotBlank(message = "링크 주소는 필수입니다.")
        @Size(max = 500, message = "링크 주소는 500자 이하여야 합니다.")
        String url
) {
}
