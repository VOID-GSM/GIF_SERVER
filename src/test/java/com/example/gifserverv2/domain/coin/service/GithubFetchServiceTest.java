package com.example.gifserverv2.domain.coin.service;

import com.example.gifserverv2.domain.coin.service.GithubFetchService.PushedCommit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GithubFetchServiceTest {

    static final Instant SINCE = Instant.parse("2026-11-01T00:00:00Z");
    static final String EVENTS = "https://api.github.com/users/octocat/events?per_page=100&page=1";

    MockRestServiceServer server;
    GithubFetchService service;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new GithubFetchService(restTemplate);
    }

    @Test
    void develop_브랜치_푸시의_본인_커밋을_모으고_남의_커밋과_머지_커밋과_연동_전_커밋은_뺀다() {
        expectJson(EVENTS, "[" + push("2026-11-06T09:00:00Z", "VOID-GSM/GIF_WEB", "aaa", "bbb") + "]");
        expectJson("https://api.github.com/repos/VOID-GSM/GIF_WEB/compare/aaa...bbb", """
                {"commits": [
                  %s, %s, %s, %s, %s
                ]}""".formatted(
                commit("c1", "octocat", "2026-11-06T08:00:00Z", 1),
                commit("c2", "OctoCat", "2026-11-06T08:10:00Z", 1),
                commit("other", "someone", "2026-11-06T08:20:00Z", 1),
                commit("merge", "octocat", "2026-11-06T08:30:00Z", 2),
                commit("old", "octocat", "2026-10-20T08:00:00Z", 1)));

        List<PushedCommit> commits = service.fetchPushedCommits("octocat", "token", SINCE);

        assertThat(commits).extracting(PushedCommit::sha).containsExactly("c1", "c2");
        assertThat(commits.get(0).fingerprint()).hasSize(64);
        server.verify();
    }

    @Test
    void 새_브랜치_푸시는_기본_브랜치와_비교하고_since_이전_이벤트는_보지_않는다() {
        expectJson(EVENTS, "[" + String.join(",",
                push("2026-11-07T09:00:00Z", "VOID-GSM/GIF_SERVER", "0000000000000000000000000000000000000000", "head1"),
                """
                {"type": "WatchEvent", "created_at": "2026-11-05T00:00:00Z", "repo": {"name": "a/b"}, "payload": {}}""",
                push("2026-10-30T09:00:00Z", "VOID-GSM/GIF_SERVER", "x", "y")) + "]");
        expectJson("https://api.github.com/repos/VOID-GSM/GIF_SERVER", "{\"default_branch\": \"main\"}");
        expectJson("https://api.github.com/repos/VOID-GSM/GIF_SERVER/compare/main...head1",
                "{\"commits\": [" + commit("n1", "octocat", "2026-11-07T08:00:00Z", 1) + "]}");

        List<PushedCommit> commits = service.fetchPushedCommits("octocat", "token", SINCE);

        assertThat(commits).extracting(PushedCommit::sha).containsExactly("n1");
        server.verify();
    }

    @Test
    void 같은_커밋이_여러_푸시에_나와도_한_번만_돌려준다() {
        expectJson(EVENTS, "[" + push("2026-11-07T09:00:00Z", "o/r", "b", "c") + ","
                + push("2026-11-06T09:00:00Z", "o/r", "a", "b") + "]");
        expectJson("https://api.github.com/repos/o/r/compare/b...c",
                "{\"commits\": [" + commit("same", "octocat", "2026-11-06T08:00:00Z", 1) + "]}");
        expectJson("https://api.github.com/repos/o/r/compare/a...b",
                "{\"commits\": [" + commit("same", "octocat", "2026-11-06T08:00:00Z", 1) + "]}");

        assertThat(service.fetchPushedCommits("octocat", "token", SINCE)).extracting(PushedCommit::sha)
                .containsExactly("same");
    }

    @Test
    void 비교할_커밋이_사라진_푸시는_건너뛴다() {
        expectJson(EVENTS, "[" + push("2026-11-07T09:00:00Z", "o/deleted", "a", "b") + "]");
        server.expect(requestTo("https://api.github.com/repos/o/deleted/compare/a...b"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(service.fetchPushedCommits("octocat", "token", SINCE)).isEmpty();
    }

    @Test
    void GitHub_API_호출에_실패하면_502를_던진다() {
        server.expect(requestTo(EVENTS)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> service.fetchPushedCommits("octocat", "expired", SINCE))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.getReason()).contains("다시 연동");
                });
    }

    @Test
    void 호출_한도를_넘으면_한도_초과_안내와_함께_502를_던진다() {
        server.expect(requestTo(EVENTS)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> service.fetchPushedCommits("octocat", "token", SINCE))
                .isInstanceOfSatisfying(ResponseStatusException.class, e -> {
                    assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
                    assertThat(e.getReason()).contains("한도");
                });
    }

    private void expectJson(String url, String body) {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    private static String push(String createdAt, String repo, String before, String head) {
        return """
                {"type": "PushEvent", "created_at": "%s", "repo": {"name": "%s"},
                 "payload": {"ref": "refs/heads/develop", "before": "%s", "head": "%s"}}""".formatted(createdAt, repo, before, head);
    }

    private static String commit(String sha, String login, String committedAt, int parentCount) {
        String parents = String.join(",", Collections.nCopies(parentCount, "{\"sha\": \"p\"}"));
        return """
                {"sha": "%s", "author": {"login": "%s"}, "parents": [%s],
                 "commit": {"message": "feat: %s", "author": {"date": "%s"}, "committer": {"date": "%s"}}}"""
                .formatted(sha, login, parents, sha, committedAt, committedAt);
    }
}
