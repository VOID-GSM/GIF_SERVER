package com.example.gifserverv2.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "GitHub 연동 요청")
public record GithubConnectRequest(
        @Schema(description = "GitHub OAuth 인가 코드", example = "a1b2c3d4e5")
        @NotBlank String code
) {}