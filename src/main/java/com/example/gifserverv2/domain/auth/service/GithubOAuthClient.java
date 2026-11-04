package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.GithubUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class GithubOAuthClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;

    @Value("${oauth.github.client-id:}")
    private String clientId;

    @Value("${oauth.github.client-secret:}")
    private String clientSecret;

    @Autowired
    public GithubOAuthClient(RestClient.Builder restClientBuilder) {
        this(restClientBuilder.requestFactory(timeoutRequestFactory()).build());
    }

    GithubOAuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private static SimpleClientHttpRequestFactory timeoutRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }

    public String getAccessToken(String code) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            log.error("GitHub OAuth 설정 누락: OAUTH_GITHUB_CLIENT_ID / OAUTH_GITHUB_CLIENT_SECRET 환경변수를 확인하세요.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GitHub 연동 설정이 되어 있지 않습니다.");
        }

        Map<?, ?> response;
        try {
            response = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "code", code
                    ))
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.warn("GitHub Access Token 요청 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 인증 서버와의 통신에 실패했습니다.", e);
        }

        if (response != null && response.get("access_token") instanceof String accessToken && !accessToken.isBlank()) {
            return accessToken;
        }

        // GitHub 는 토큰 발급 실패도 200 + {"error": ...} 로 응답한다.
        Object error = response != null ? response.get("error") : null;
        Object description = response != null ? response.get("error_description") : null;
        log.warn("GitHub Access Token 발급 실패: error={}, description={}", error, description);

        if ("bad_verification_code".equals(error)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "GitHub 인증 코드가 만료되었거나 이미 사용되었습니다. 다시 연동해 주세요.");
        }
        // incorrect_client_credentials, redirect_uri_mismatch 등 서버 설정 문제
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub Access Token을 발급받지 못했습니다.");
    }

    public GithubUserInfo getUserInfo(String accessToken) {
        GithubUserInfo userInfo;
        try {
            userInfo = restClient.get()
                    .uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(GithubUserInfo.class);
        } catch (RestClientResponseException e) {
            log.warn("GitHub 사용자 정보 조회 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 사용자 정보를 가져오는 데 실패했습니다.", e);
        } catch (RestClientException e) {
            log.warn("GitHub 사용자 정보 조회 실패: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 사용자 정보를 가져오는 데 실패했습니다.", e);
        }

        if (userInfo == null || userInfo.login() == null) {
            log.warn("GitHub 사용자 정보 응답이 비어 있습니다.");
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 사용자 정보를 가져오는 데 실패했습니다.");
        }
        return userInfo;
    }
}
