package com.example.gifserverv2.domain.coin.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GitHub 에서 사용자가 푸시한 커밋을 모은다. 기본 브랜치뿐 아니라 모든 브랜치의 푸시가 대상이다. (#250)
 *
 * <p>커밋 검색(/search/commits)은 저장소의 기본 브랜치만 검색해서 develop·feat/* 커밋이 빠진다.
 * 그래서 Events API 의 PushEvent 로 푸시를 찾는다. 2025-10-07 부터 PushEvent payload 에 commits 가 없으므로,
 * 푸시마다 before...head 를 Compare API 로 조회해 커밋을 얻는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubFetchService {

    private static final String API = "https://api.github.com";
    // Events API 는 최근 90일, 최대 300개(100개씩 3페이지)까지만 준다
    private static final int EVENTS_PER_PAGE = 100;
    private static final int MAX_EVENT_PAGES = 3;
    private static final String NULL_SHA = "0000000000000000000000000000000000000000";

    private final RestTemplate restTemplate;

    public record PushedCommit(String sha, String fingerprint) {
    }

    public List<PushedCommit> fetchPushedCommits(String githubUsername, String accessToken, Instant since) {
        Map<String, PushedCommit> commits = new LinkedHashMap<>();
        Map<String, String> defaultBranches = new HashMap<>();
        for (Push push : fetchPushes(githubUsername, accessToken, since)) {
            if (NULL_SHA.equals(push.head())) {
                continue;
            }
            // 새 브랜치를 처음 푸시하면 before 가 0000… 이라 기본 브랜치와 비교한다
            String base = push.before() == null || NULL_SHA.equals(push.before())
                    ? defaultBranches.computeIfAbsent(push.repo(), repo -> fetchDefaultBranch(repo, accessToken))
                    : push.before();
            JsonNode compare = fetchCompare(push.repo(), base, push.head(), accessToken);
            if (compare == null) {
                continue;
            }
            for (JsonNode commit : compare.path("commits")) {
                if (isRewardable(commit, githubUsername, since)) {
                    String sha = commit.path("sha").asText();
                    commits.putIfAbsent(sha, new PushedCommit(sha, fingerprint(commit)));
                }
            }
        }
        return new ArrayList<>(commits.values());
    }

    private record Push(String repo, String before, String head) {
    }

    private List<Push> fetchPushes(String githubUsername, String accessToken, Instant since) {
        List<Push> pushes = new ArrayList<>();
        for (int page = 1; page <= MAX_EVENT_PAGES; page++) {
            // 토큰 주인 본인의 이벤트를 조회하면 비공개 저장소 이벤트도 포함된다
            JsonNode events = get(API + "/users/{username}/events?per_page={perPage}&page={page}", accessToken,
                    githubUsername, EVENTS_PER_PAGE, page);
            if (events == null || !events.isArray()) {
                break;
            }
            for (JsonNode event : events) {
                // 최신순이라 since 보다 오래된 이벤트가 나오면 그 뒤는 볼 필요가 없다
                if (Instant.parse(event.path("created_at").asText()).isBefore(since)) {
                    return pushes;
                }
                if ("PushEvent".equals(event.path("type").asText())) {
                    JsonNode payload = event.path("payload");
                    pushes.add(new Push(event.path("repo").path("name").asText(),
                            payload.path("before").asText(null), payload.path("head").asText()));
                }
            }
            if (events.size() < EVENTS_PER_PAGE) {
                break;
            }
        }
        return pushes;
    }

    private String fetchDefaultBranch(String repo, String accessToken) {
        String[] ownerAndName = repo.split("/", 2);
        JsonNode repository = get(API + "/repos/{owner}/{repo}", accessToken, ownerAndName[0], ownerAndName[1]);
        return repository == null ? null : repository.path("default_branch").asText(null);
    }

    /** 저장소·브랜치가 지워졌거나 force push 로 before 커밋이 사라졌으면 null (그 푸시는 건너뛴다) */
    private JsonNode fetchCompare(String repo, String base, String head, String accessToken) {
        if (base == null) {
            return null;
        }
        String[] ownerAndName = repo.split("/", 2);
        try {
            return get(API + "/repos/{owner}/{repo}/compare/{basehead}", accessToken,
                    ownerAndName[0], ownerAndName[1], base + "..." + head);
        } catch (ResponseStatusException e) {
            if (e.getCause() instanceof HttpStatusCodeException cause
                    && (cause.getStatusCode().value() == 404 || cause.getStatusCode().value() == 422)) {
                log.info("GitHub 커밋 비교를 건너뜁니다 (repo: {}, {}...{}): {}", repo, base, head, cause.getStatusCode());
                return null;
            }
            throw e;
        }
    }

    private static boolean isRewardable(JsonNode commit, String githubUsername, Instant since) {
        // GitHub 계정과 연결되지 않은 이메일로 만든 커밋은 author 가 null 이다
        String authorLogin = commit.path("author").path("login").asText(null);
        if (authorLogin == null || !authorLogin.equalsIgnoreCase(githubUsername)) {
            return false;
        }
        if (commit.path("parents").size() > 1) {
            return false;
        }
        // 연동 전에 만든 커밋은 지급하지 않는다 (새 브랜치를 기본 브랜치와 비교하면 예전 커밋도 섞여 나온다)
        String committedAt = commit.path("commit").path("committer").path("date").asText(null);
        return committedAt != null && !Instant.parse(committedAt).isBefore(since);
    }

    private static String fingerprint(JsonNode commit) {
        JsonNode detail = commit.path("commit");
        String source = detail.path("author").path("date").asText() + "\n" + detail.path("message").asText();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private JsonNode get(String url, String accessToken, Object... uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        if (accessToken != null && !accessToken.isBlank()) {
            headers.setBearerAuth(accessToken);
        }
        try {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class, uriVariables)
                    .getBody();
        } catch (HttpStatusCodeException e) {
            log.warn("GitHub API 호출 실패 ({}): status={}, body={}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, failureMessage(e.getStatusCode().value()), e);
        } catch (RestClientException e) {
            log.warn("GitHub API 호출 실패 ({}): {}", url, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 서버와 통신에 실패했습니다. 잠시 후 다시 시도해 주세요.", e);
        }
    }

    private static String failureMessage(int status) {
        return switch (status) {
            case 401 -> "GitHub 연동이 만료되었습니다. GitHub 계정을 다시 연동해 주세요.";
            case 403, 429 -> "GitHub API 호출 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.";
            default -> "GitHub에서 커밋 정보를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요.";
        };
    }
}
