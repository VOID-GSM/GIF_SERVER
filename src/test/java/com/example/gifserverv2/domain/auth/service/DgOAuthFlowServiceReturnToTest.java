package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.store.InMemoryOAuthStateStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import team.themoment.datagsm.sdk.oauth.DataGsmOAuthClient;
import team.themoment.datagsm.sdk.oauth.model.AuthorizationUrlBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 로그인 시작 시 받은 returnTo 가 state 에 담겨 콜백 응답까지 전달되는지 확인한다.
 */
class DgOAuthFlowServiceReturnToTest {

    private static final String REDIRECT_URI = "https://gif.example.com/auth/callback";

    private final DataGsmOAuthClient dataGsmOAuthClient = mock(DataGsmOAuthClient.class);
    private final AuthorizationUrlBuilder urlBuilder = mock(AuthorizationUrlBuilder.class, RETURNS_SELF);
    private final AuthService authService = mock(AuthService.class);
    private DgOAuthFlowService flowService;

    @BeforeEach
    void setUp() {
        flowService = new DgOAuthFlowService(dataGsmOAuthClient, authService, new InMemoryOAuthStateStore());
        given(dataGsmOAuthClient.createAuthorizationUrl(REDIRECT_URI)).willReturn(urlBuilder);
        given(urlBuilder.getCodeVerifier()).willReturn("verifier");
        given(urlBuilder.build()).willReturn("https://datagsm.example.com/authorize");
        given(authService.signIn("code", REDIRECT_URI, "verifier")).willReturn(signedIn());
    }

    @Test
    void 허용된_복귀_경로는_콜백_응답에_담긴다() {
        String state = startLogin("/projects/12?tab=comments");

        OAuthSignInResponse response = flowService.completeLogin("code", state);

        assertThat(response.returnTo()).isEqualTo("/projects/12?tab=comments");
        assertThat(response.accessToken()).isEqualTo("jwt");
    }

    @Test
    void 외부_도메인_복귀_경로는_버리고_로그인은_진행한다() {
        String state = startLogin("https://evil.com");

        OAuthSignInResponse response = flowService.completeLogin("code", state);

        assertThat(response.returnTo()).isNull();
        assertThat(response.accessToken()).isEqualTo("jwt");
    }

    @Test
    void 복귀_경로_없이_시작하면_기존처럼_null() {
        String state = startLogin(null);

        assertThat(flowService.completeLogin("code", state).returnTo()).isNull();
    }

    private String startLogin(String returnTo) {
        flowService.createLoginRedirect(REDIRECT_URI, returnTo);
        ArgumentCaptor<String> state = ArgumentCaptor.forClass(String.class);
        verify(urlBuilder).state(state.capture());
        return state.getValue();
    }

    private OAuthSignInResponse signedIn() {
        return new OAuthSignInResponse("jwt", 1L, "s@gsm.hs.kr", "학생", "1101", "1", "USER",
                "STUDENT", null, null, false, null, null);
    }
}
