package com.example.gifserverv2.domain.coin.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoinSyncService {

    private final GithubFetchService githubFetchService;
    private final CoinAccountRepository coinAccountRepository;
    private final UserRepository userRepository;

    private static final int COINS_PER_COMMIT = 10;

    @Transactional
    public int syncCommitCoins(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String githubUsername = user.getGithubUsername();
        if (githubUsername == null || githubUsername.isBlank()) {
            throw new IllegalStateException("연동된 GitHub 계정이 없습니다. 마이페이지에서 GitHub 아이디를 먼저 등록해 주세요.");
        }

        String githubAccessToken = user.getGithubAccessToken();
        int totalCommitCount = githubFetchService.getCommitCount(githubUsername, githubAccessToken);

        CoinAccount coinAccount = coinAccountRepository.findByUserId(userId)
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(userId)));

        int initialCommitCount = user.getInitialCommitCount();
        int lastSyncedCount = coinAccount.getLastSyncedCommitCount();

        int baseCommitCount = Math.max(initialCommitCount, lastSyncedCount);
        int newCommits = totalCommitCount - baseCommitCount;

        if (newCommits <= 0) {
            return 0;
        }

        int rewardCoins = newCommits * COINS_PER_COMMIT;
        coinAccount.syncCommits(totalCommitCount, rewardCoins);

        user.updateCoinBalance(coinAccount.getBalance());

        return rewardCoins;
    }
}