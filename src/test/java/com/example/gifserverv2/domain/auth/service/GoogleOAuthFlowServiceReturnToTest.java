package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.store.InMemoryOAuthStateStore;
import com.example.gifserverv2.global.config.OAuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Google 로그인 흐름에서 returnTo 가 state 에 담겨 콜백 응답까지 전달되는지 확인한다.
 */
class GoogleOAuthFlowServiceReturnToTest {

    private static final String REDIRECT_URI = "https://gif.example.com/auth/google/callback";
    private static final String TOKEN_URI = "https://oauth2.example.com/token";
    private static final String USER_INFO_URI = "https://oauth2.example.com/userinfo";

    private final AuthService authService = mock(AuthService.class);
    private GoogleOAuthFlowService flowService;
    private MockRestServiceServer googleServer;

    @BeforeEach
    void setUp() {
        OAuthProperties oauthProperties = new OAuthProperties();
        oauthProperties.getGoogle().setRedirectUris(List.of(REDIRECT_URI));

        flowService = new GoogleOAuthFlowService(authService, new InMemoryOAuthStateStore(), oauthProperties);
        ReflectionTestUtils.setField(flowService, "clientId", "client-id");
        ReflectionTestUtils.setField(flowService, "clientSecret", "client-secret");
        ReflectionTestUtils.setField(flowService, "authorizationUri", "https://accounts.example.com/auth");
        ReflectionTestUtils.setField(flowService, "tokenUri", TOKEN_URI);
        ReflectionTestUtils.setField(flowService, "userInfoUri", USER_INFO_URI);
        ReflectionTestUtils.setField(flowService, "scopes", "openid email profile");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(flowService, "restTemplate");
        googleServer = MockRestServiceServer.bindTo(restTemplate).build();

        given(authService.signInWithGoogle(eq("google-access-token"), anyMap())).willReturn(signedIn());
    }

    @Test
    void 허용된_복귀_경로는_콜백_응답에_담긴다() {
        String state = startLogin("/projects/12?tab=comments");
        expectGoogleLogin();

        OAuthSignInResponse response = flowService.completeLogin("code", state);

        assertThat(response.returnTo()).isEqualTo("/projects/12?tab=comments");
        assertThat(response.accessToken()).isEqualTo("jwt");
        googleServer.verify();
    }

    @Test
    void 외부_도메인_복귀_경로는_버리고_로그인은_진행한다() {
        String state = startLogin("https://evil.com");
        expectGoogleLogin();

        OAuthSignInResponse response = flowService.completeLogin("code", state);

        assertThat(response.returnTo()).isNull();
        assertThat(response.accessToken()).isEqualTo("jwt");
    }

    @Test
    void 복귀_경로_없이_시작하면_기존처럼_null() {
        String state = startLogin(null);
        expectGoogleLogin();

        assertThat(flowService.completeLogin("code", state).returnTo()).isNull();
    }

    private String startLogin(String returnTo) {
        URI location = flowService.createLoginRedirect(REDIRECT_URI, returnTo);
        return UriComponentsBuilder.fromUri(location).build().getQueryParams().getFirst("state");
    }

    private void expectGoogleLogin() {
        googleServer.expect(requestTo(TOKEN_URI)).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"google-access-token\"}", MediaType.APPLICATION_JSON));
        googleServer.expect(requestTo(USER_INFO_URI)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"email\":\"guest@gmail.com\",\"name\":\"게스트\"}", MediaType.APPLICATION_JSON));
    }

    private OAuthSignInResponse signedIn() {
        return new OAuthSignInResponse("jwt", 2L, "guest@gmail.com", "게스트", null, null, "EXTERNAL",
                "EXTERNAL", null, null, false, null, null);
    }
}
