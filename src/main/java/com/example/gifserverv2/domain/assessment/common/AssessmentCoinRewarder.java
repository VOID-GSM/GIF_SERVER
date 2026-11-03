package com.example.gifserverv2.domain.assessment.common;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 역량검사 보상 코인을 지급한다. CoinAccount 와 함께 프로필에 보이는 UserEntity.coinBalance 도 맞춰 갱신한다.
 */
@Component
@RequiredArgsConstructor
public class AssessmentCoinRewarder {

    private final CoinAccountRepository coinAccountRepository;
    private final UserRepository userRepository;

    public void reward(Long userId, int amount) {
        if (amount <= 0) {
            return;
        }
        CoinAccount account = coinAccountRepository.findByUserId(userId)
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(userId)));
        account.addCoin(amount);
        userRepository.findById(userId)
                .ifPresent(user -> user.updateCoinBalance(account.getBalance()));
    }
}
