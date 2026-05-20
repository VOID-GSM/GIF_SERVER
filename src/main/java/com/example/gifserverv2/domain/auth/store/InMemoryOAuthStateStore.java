package com.example.gifserverv2.domain.auth.store;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "app.oauth-state", name = "store", havingValue = "memory", matchIfMissing = true)
public class InMemoryOAuthStateStore implements OAuthStateStore {

    private final Map<String, StoredLoginState> stateStore = new ConcurrentHashMap<>();
    private final DelayQueue<ExpiringState> expirationQueue = new DelayQueue<>();

    @Override
    public void save(String state, String redirectUri, String codeVerifier, Duration ttl) {
        cleanupExpiredStates();

        Instant expiresAt = Instant.now().plus(ttl);
        stateStore.put(state, new StoredLoginState(redirectUri, codeVerifier, expiresAt));
        expirationQueue.offer(new ExpiringState(state, expiresAt));
    }

    @Override
    public Optional<LoginState> consume(String state) {
        cleanupExpiredStates();

        StoredLoginState stored = stateStore.remove(state);
        if (stored == null || stored.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }

        return Optional.of(new LoginState(stored.redirectUri(), stored.codeVerifier()));
    }

    private void cleanupExpiredStates() {
        ExpiringState expired;
        while ((expired = expirationQueue.poll()) != null) {
            ExpiringState expiredState = expired;
            stateStore.computeIfPresent(
                    expiredState.state(),
                    (key, current) -> current.expiresAt().equals(expiredState.expiresAt()) ? null : current
            );
        }
    }

    private record StoredLoginState(String redirectUri, String codeVerifier, Instant expiresAt) {
    }

    private record ExpiringState(String state, Instant expiresAt) implements Delayed {

        @Override
        public long getDelay(TimeUnit unit) {
            long remainingMillis = expiresAt.toEpochMilli() - System.currentTimeMillis();
            return unit.convert(remainingMillis, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            if (other instanceof ExpiringState otherState) {
                return this.expiresAt.compareTo(otherState.expiresAt);
            }
            return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));
        }
    }
}