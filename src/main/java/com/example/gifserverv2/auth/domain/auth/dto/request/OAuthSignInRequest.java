package com.example.gifserverv2.auth.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OAuthSignInRequest (
    @NotBlank String authCode,
    @NotBlank String redirectUri,
    @NotBlank String codeVerifier) {
}
