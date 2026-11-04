package com.example.gifserverv2.domain.auth.store;

import java.time.Duration;
import java.util.Optional;

public interface OAuthStateStore {

    void save(String state, String redirectUri, String codeVerifier, String returnTo, Duration ttl);

    Optional<LoginState> consume(String state);

    /**
     * @param returnTo 로그인 후 복귀할 프론트 경로 (없으면 null)
     */
    record LoginState(String redirectUri, String codeVerifier, String returnTo) {
    }
}