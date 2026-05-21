package com.example.gifserverv2.domain.auth.store;

import java.time.Duration;
import java.util.Optional;

public interface OAuthStateStore {

    void save(String state, String redirectUri, String codeVerifier, Duration ttl);

    Optional<LoginState> consume(String state);

    record LoginState(String redirectUri, String codeVerifier) {
    }
}