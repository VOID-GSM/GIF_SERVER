package com.example.gifserverv2.domain.coin.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CoinSyncServiceTest {

    @Mock GithubFetchService githubFetchService;
    @Mock CoinAccountRepository coinAccountRepository;
    @Mock UserRepository userRepository;
    @InjectMocks CoinSyncService service;

    private UserEntity user;
    private CoinAccount account;

    @BeforeEach
    void setUp() {
        user = new UserEntity("student@gsm.hs.kr", "홍길동", "2101", Role.USER);
        user.connectGithub("octocat", null, "token", 5);
        account = CoinAccount.open(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(coinAccountRepository.findByUserId(1L)).willReturn(Optional.of(account));
    }

    @Test
    void 새_커밋_3개면_30코인을_얻고_잔액에_반영된다() {
        given(githubFetchService.getCommitCount("octocat", "token")).willReturn(8);

        int earned = service.syncCommitCoins(1L);

        assertThat(earned).isEqualTo(30);
        assertThat(account.getBalance()).isEqualTo(30);
        assertThat(user.getCoinBalance()).isEqualTo(30);
        assertThat(account.getLastSyncedCommitCount()).isEqualTo(8);
    }

    @Test
    void 새_커밋이_없으면_0코인이고_잔액은_그대로다() {
        given(githubFetchService.getCommitCount("octocat", "token")).willReturn(8);
        service.syncCommitCoins(1L);

        int earned = service.syncCommitCoins(1L);

        assertThat(earned).isZero();
        assertThat(account.getBalance()).isEqualTo(30);
        assertThat(user.getCoinBalance()).isEqualTo(30);
    }
}
