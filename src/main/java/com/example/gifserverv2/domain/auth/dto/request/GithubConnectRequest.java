package com.example.gifserverv2.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GithubConnectRequest(
        @NotBlank String code
) {}