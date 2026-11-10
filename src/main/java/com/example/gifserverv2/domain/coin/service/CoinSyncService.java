package com.example.gifserverv2.domain.coin.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.entity.RewardedCommit;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.coin.repository.RewardedCommitRepository;
import com.example.gifserverv2.domain.coin.service.GithubFetchService.PushedCommit;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CoinSyncService {

    private final GithubFetchService githubFetchService;
    private final CoinAccountRepository coinAccountRepository;
    private final RewardedCommitRepository rewardedCommitRepository;
    private final UserRepository userRepository;

    private static final int COINS_PER_COMMIT = 10;
    // Events API 는 푸시가 몇 분~몇 시간 늦게 보이기도 해서, 마지막 동기화보다 조금 앞부터 다시 본다. 이미 지급한 커밋은 건너뛴다
    private static final Duration EVENT_DELAY_MARGIN = Duration.ofHours(6);

    @Transactional
    public int syncCommitCoins(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String githubUsername = user.getGithubUsername();
        if (githubUsername == null || githubUsername.isBlank()) {
            throw new IllegalStateException("연동된 GitHub 계정이 없습니다. 마이페이지에서 GitHub 아이디를 먼저 등록해 주세요.");
        }

        // 동시에 동기화하면 같은 커밋을 두 번 지급할 수 있어 계정 행을 잠근 채 진행한다
        CoinAccount coinAccount = coinAccountRepository.findByUserId(userId)
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(userId)));

        Instant now = Instant.now();
        Instant connectedAt = user.getGithubConnectedAt();
        if (connectedAt == null) {
            // 연동 시각 백필 전에 연동한 계정이면 지금부터 집계한다
            user.markGithubConnectedAt(now);
            coinAccount.syncCommits(0, now);
            return 0;
        }

        List<PushedCommit> pushed = githubFetchService.fetchPushedCommits(
                githubUsername, user.getGithubAccessToken(), syncSince(connectedAt, coinAccount.getLastCommitSyncedAt()));
        List<PushedCommit> newCommits = excludeRewarded(userId, pushed);

        rewardedCommitRepository.saveAll(newCommits.stream()
                .map(commit -> new RewardedCommit(userId, commit.sha(), commit.fingerprint(), now))
                .toList());
        int rewardCoins = newCommits.size() * COINS_PER_COMMIT;
        coinAccount.syncCommits(rewardCoins, now);
        user.updateCoinBalance(coinAccount.getBalance());

        return rewardCoins;
    }

    private static Instant syncSince(Instant connectedAt, Instant lastSyncedAt) {
        if (lastSyncedAt == null) {
            return connectedAt;
        }
        Instant withMargin = lastSyncedAt.minus(EVENT_DELAY_MARGIN);
        return withMargin.isAfter(connectedAt) ? withMargin : connectedAt;
    }

    private List<PushedCommit> excludeRewarded(Long userId, List<PushedCommit> pushed) {
        if (pushed.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new HashSet<>();
        rewardedCommitRepository.findAlreadyRewarded(userId,
                        pushed.stream().map(PushedCommit::sha).toList(),
                        pushed.stream().map(PushedCommit::fingerprint).toList())
                .forEach(rewarded -> {
                    seen.add(rewarded.getSha());
                    seen.add(rewarded.getFingerprint());
                });
        List<PushedCommit> newCommits = new ArrayList<>();
        for (PushedCommit commit : pushed) {
            if (seen.contains(commit.sha()) || seen.contains(commit.fingerprint())) {
                continue;
            }
            seen.add(commit.sha());
            seen.add(commit.fingerprint());
            newCommits.add(commit);
        }
        return newCommits;
    }
}
