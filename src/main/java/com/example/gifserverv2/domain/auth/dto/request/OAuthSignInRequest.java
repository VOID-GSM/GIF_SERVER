package com.example.gifserverv2.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "DataGSM 로그인 요청 (PKCE 직접 처리)")
public record OAuthSignInRequest (
    @Schema(description = "DataGSM에서 받은 인가 코드", example = "AbCdEf123")
    @NotBlank String authCode,
    @Schema(description = "인가 요청에 사용한 redirectUri. 서버에 등록된 주소여야 한다", example = "https://gif.example.com/auth/callback")
    @NotBlank String redirectUri,
    @Schema(description = "인가 요청 시 만든 PKCE code_verifier", example = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk")
    @NotBlank String codeVerifier) {
}
