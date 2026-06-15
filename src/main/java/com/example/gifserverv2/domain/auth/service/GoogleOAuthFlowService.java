package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.store.OAuthStateStore;
import org.springframework.beans.factory.annotation.Value;
import com.example.gifserverv2.global.config.OAuthProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleOAuthFlowService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthService authService;
    private final OAuthStateStore oauthStateStore;
    private final OAuthProperties oauthProperties;

    @Value("${oauth.google.client-id:}")
    private String clientId;

    @Value("${oauth.google.client-secret:}")
    private String clientSecret;

    @Value("${oauth.google.authorization-uri:https://accounts.google.com/o/oauth2/v2/auth}")
    private String authorizationUri;

    @Value("${oauth.google.token-uri:https://oauth2.googleapis.com/token}")
    private String tokenUri;

    @Value("${oauth.google.userinfo-uri:https://openidconnect.googleapis.com/v1/userinfo}")
    private String userInfoUri;

    @Value("${oauth.google.scopes:openid email profile}")
    private String scopes;

    public GoogleOAuthFlowService(AuthService authService, OAuthStateStore oauthStateStore, OAuthProperties oauthProperties) {
        this.authService = authService;
        this.oauthStateStore = oauthStateStore;
        this.oauthProperties = oauthProperties;
    }

    public URI createLoginRedirect(String redirectUri) {
        // enforce allowed redirect URIs for Google (list from application.yml / env)
        if (oauthProperties.getGoogle().getRedirectUris() == null || oauthProperties.getGoogle().getRedirectUris().isEmpty()) {
            // fall back to global check (datagsm) if google list not configured
            authService.assertAllowedRedirectUri(redirectUri);
        } else {
            if (!oauthProperties.getGoogle().getRedirectUris().contains(redirectUri)) {
                throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "허용되지 않은 redirectUri입니다.");
            }
        }

        String state = UUID.randomUUID().toString();
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = createCodeChallenge(codeVerifier);

        String url = authorizationUri + "?"
                + "client_id=" + urlEncode(clientId)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&response_type=code"
                + "&scope=" + urlEncode(scopes)
                + "&state=" + urlEncode(state)
                + "&code_challenge=" + urlEncode(codeChallenge)
                + "&code_challenge_method=S256"
                + "&access_type=offline"
                + "&prompt=consent";

        oauthStateStore.save(state, redirectUri, codeVerifier, STATE_TTL);
        return URI.create(url);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public OAuthSignInResponse completeLogin(String code, String state) {
        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "code/state 값이 필요합니다.");
        }

        OAuthStateStore.LoginState loginState = oauthStateStore.consume(state)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "state가 유효하지 않거나 만료되었습니다."));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        if (clientSecret != null && !clientSecret.isBlank()) {
            body.add("client_secret", clientSecret);
        }
        body.add("redirect_uri", loginState.redirectUri());
        body.add("grant_type", "authorization_code");
        if (loginState.codeVerifier() != null) {
            body.add("code_verifier", loginState.codeVerifier());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, request, Map.class);
        Map tokenBody = tokenResponse.getBody();
        if (tokenBody == null || tokenBody.get("access_token") == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "토큰 교환에 실패했습니다.");
        }

        String accessToken = tokenBody.get("access_token").toString();

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userReq = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userInfoResp = restTemplate.exchange(userInfoUri, org.springframework.http.HttpMethod.GET, userReq, Map.class);
        Map userInfo = userInfoResp.getBody();
        if (userInfo == null || userInfo.get("email") == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_GATEWAY, "사용자 정보를 가져오지 못했습니다.");
        }

        return authService.signInWithGoogle(accessToken, userInfo);
    }

    private static String urlEncode(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String generateCodeVerifier() {
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private static String createCodeChallenge(String codeVerifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}