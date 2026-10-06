package com.example.gifserverv2.domain.coin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubFetchService {

    private final RestTemplate restTemplate = new RestTemplate();

    public int getCommitCount(String githubUsername, String accessToken) {
        String url = String.format("https://api.github.com/search/commits?q=author:%s", githubUsername);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github.cloak-preview+json");

            if (accessToken != null && !accessToken.isBlank()) {
                headers.set("Authorization", "Bearer " + accessToken);
            }

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("total_count")) {
                return (Integer) body.get("total_count");
            }

            return 0;
        } catch (Exception e) {
            log.error("GitHub API 커밋 수 조회 실패 (username: {}): {}", githubUsername, e.getMessage());
            return 0;
        }
    }
}