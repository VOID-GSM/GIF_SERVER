package com.example.gifserverv2.domain.retrospect.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로젝트 회고록 작성·수정 요청")
public record WriteRetrospectRequest(
        @Schema(description = "제목 (최대 100자)", example = "GIF 프로젝트를 마치며")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "내용 (최대 10000자)", example = "기획부터 배포까지...")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 10000, message = "내용은 10000자를 초과할 수 없습니다.")
        String content
) {}
