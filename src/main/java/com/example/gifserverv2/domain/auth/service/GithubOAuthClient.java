package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.GithubUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class GithubOAuthClient {

    private final RestClient restClient;

    @Value("${oauth.github.client-id:}")
    private String clientId;

    @Value("${oauth.github.client-secret:}")
    private String clientSecret;

    public GithubOAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String getAccessToken(String code) {
        try {
            Map response = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code
                    ))
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("access_token")) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GitHub Access Token을 발급받지 못했습니다.");
            }

            return (String) response.get("access_token");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 인증 서버와의 통신에 실패했습니다.", e);
        }
    }

    public GithubUserInfo getUserInfo(String accessToken) {
        try {
            return restClient.get()
                    .uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(GithubUserInfo.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 사용자 정보를 가져오는 데 실패했습니다.", e);
        }
    }
}