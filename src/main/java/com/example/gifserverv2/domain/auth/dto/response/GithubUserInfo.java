package com.example.gifserverv2.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUserInfo(
        String login,
        Long id,
        @JsonProperty("avatar_url")
        String avatarUrl,
        String email
) {
}