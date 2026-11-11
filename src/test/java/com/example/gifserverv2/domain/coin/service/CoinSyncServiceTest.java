package com.example.gifserverv2.domain.coin.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.coin.repository.RewardedCommitRepository;
import com.example.gifserverv2.domain.coin.service.GithubFetchService.PushedCommit;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(CoinSyncService.class)
class CoinSyncServiceTest {

    @Autowired CoinSyncService service;
    @Autowired UserRepository userRepository;
    @Autowired CoinAccountRepository coinAccountRepository;
    @Autowired RewardedCommitRepository rewardedCommitRepository;
    @MockitoBean GithubFetchService githubFetchService;

    Instant connectedAt = Instant.parse("2026-11-01T00:00:00Z");
    Long userId;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity("student@gsm.hs.kr", "홍길동", "2101", Role.USER);
        user.connectGithub("octocat", null, "token", connectedAt);
        userId = userRepository.save(user).getId();
    }

    @Test
    void 아직_지급하지_않은_커밋마다_10코인을_지급하고_잔액에_반영한다() {
        given(githubFetchService.fetchPushedCommits("octocat", "token", connectedAt))
                .willReturn(List.of(commit("a"), commit("b"), commit("c")));

        int earned = service.syncCommitCoins(userId);

        assertThat(earned).isEqualTo(30);
        assertThat(coinAccountRepository.findByUserId(userId).orElseThrow().getBalance()).isEqualTo(30);
        assertThat(userRepository.findById(userId).orElseThrow().getCoinBalance()).isEqualTo(30);
        assertThat(rewardedCommitRepository.findAll()).extracting(r -> r.getSha()).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void 다시_동기화해도_이미_지급한_커밋은_다시_지급하지_않는다() {
        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any()))
                .willReturn(List.of(commit("a"), commit("b")));
        service.syncCommitCoins(userId);

        // 다른 브랜치에 같은 커밋이 푸시되고 새 커밋 하나가 더해졌다
        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any()))
                .willReturn(List.of(commit("a"), commit("b"), commit("d")));
        int earned = service.syncCommitCoins(userId);

        assertThat(earned).isEqualTo(10);
        assertThat(coinAccountRepository.findByUserId(userId).orElseThrow().getBalance()).isEqualTo(30);
    }

    @Test
    void 리베이스로_SHA만_바뀐_커밋은_다시_지급하지_않는다() {
        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any()))
                .willReturn(List.of(new PushedCommit("a", "fp-a")));
        service.syncCommitCoins(userId);

        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any()))
                .willReturn(List.of(new PushedCommit("a-rebased", "fp-a"), new PushedCommit("b", "fp-b"),
                        new PushedCommit("b-cherry-picked", "fp-b")));
        int earned = service.syncCommitCoins(userId);

        assertThat(earned).isEqualTo(10);
    }

    @Test
    void 두_번째_동기화부터는_마지막_동기화보다_6시간_앞부터_조회하되_연동_시각보다_앞서지_않는다() {
        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any())).willReturn(List.of());
        service.syncCommitCoins(userId);
        Instant lastSyncedAt = coinAccountRepository.findByUserId(userId).orElseThrow().getLastCommitSyncedAt();

        service.syncCommitCoins(userId);

        verify(githubFetchService).fetchPushedCommits("octocat", "token", connectedAt);
        verify(githubFetchService).fetchPushedCommits("octocat", "token", lastSyncedAt.minus(Duration.ofHours(6)));
    }

    @Test
    void 연동_시각이_없는_기존_계정은_지금부터_집계하고_이번에는_지급하지_않는다() {
        UserEntity legacy = new UserEntity("old@gsm.hs.kr", "김철수", "2102", Role.USER);
        legacy.connectGithub("legacy", null, "token", null);
        Long legacyId = userRepository.save(legacy).getId();

        int earned = service.syncCommitCoins(legacyId);

        assertThat(earned).isZero();
        assertThat(userRepository.findById(legacyId).orElseThrow().getGithubConnectedAt()).isNotNull();
        verify(githubFetchService, never()).fetchPushedCommits(eq("legacy"), any(), any());
    }

    @Test
    void GitHub_API_호출에_실패하면_0코인이_아니라_502를_던지고_아무것도_지급하지_않는다() {
        given(githubFetchService.fetchPushedCommits(eq("octocat"), eq("token"), any()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub 연동이 만료되었습니다."));

        assertThatThrownBy(() -> service.syncCommitCoins(userId))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY));
        assertThat(rewardedCommitRepository.findAll()).isEmpty();
    }

    @Test
    void 연동하지_않은_사용자는_동기화할_수_없다() {
        UserEntity user = userRepository.findById(userId).orElseThrow();
        user.disconnectGithub();

        assertThatThrownBy(() -> service.syncCommitCoins(userId)).isInstanceOf(IllegalStateException.class);
    }

    private static PushedCommit commit(String sha) {
        return new PushedCommit(sha, "fp-" + sha);
    }
}
