package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.store.OAuthStateStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.model.AuthorizationUrlBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
public class DgOAuthFlowService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final DataGsmOAuthClient dataGsmOAuthClient;
    private final AuthService authService;
    private final OAuthStateStore oauthStateStore;

    public DgOAuthFlowService(DataGsmOAuthClient dataGsmOAuthClient, AuthService authService,
                              OAuthStateStore oauthStateStore) {
        this.dataGsmOAuthClient = dataGsmOAuthClient;
        this.authService = authService;
        this.oauthStateStore = oauthStateStore;
    }

    public URI createLoginRedirect(String redirectUri) {
        authService.assertAllowedRedirectUri(redirectUri);

        String state = UUID.randomUUID().toString();
        AuthorizationUrlBuilder urlBuilder = dataGsmOAuthClient.createAuthorizationUrl(redirectUri)
                .state(state)
                .enablePkce();

        oauthStateStore.save(state, redirectUri, urlBuilder.getCodeVerifier(), STATE_TTL);
        return URI.create(urlBuilder.build());
    }

    public OAuthSignInResponse completeLogin(String code, String state) {
        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code/state 값이 필요합니다.");
        }

        OAuthStateStore.LoginState loginState = oauthStateStore.consume(state)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "state가 유효하지 않거나 만료되었습니다."));

        return authService.signIn(code, loginState.redirectUri(), loginState.codeVerifier());
    }
}
