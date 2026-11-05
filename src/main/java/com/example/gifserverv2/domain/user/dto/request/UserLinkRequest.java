package com.example.gifserverv2.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 개인 사이트 링크 추가/수정.
 *
 * @param title 표시 이름 (예: "블로그", 선택)
 * @param url   http/https 주소
 */
public record UserLinkRequest(
        @Size(max = 30, message = "링크 이름은 30자 이하여야 합니다.")
        String title,

        @NotBlank(message = "링크 주소는 필수입니다.")
        @Size(max = 500, message = "링크 주소는 500자 이하여야 합니다.")
        String url
) {
}
