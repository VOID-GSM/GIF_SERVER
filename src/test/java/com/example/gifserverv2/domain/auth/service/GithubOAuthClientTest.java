package com.example.gifserverv2.domain.auth.service;

import com.example.gifserverv2.domain.auth.dto.response.GithubUserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GithubOAuthClientTest {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_URL = "https://api.github.com/user";

    private MockRestServiceServer server;
    private GithubOAuthClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new GithubOAuthClient(builder.build());
        ReflectionTestUtils.setField(client, "clientId", "client-id");
        ReflectionTestUtils.setField(client, "clientSecret", "client-secret");
    }

    @Test
    void 토큰_발급에_성공하면_access_token을_반환한다() {
        server.expect(requestTo(TOKEN_URL)).andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess("{\"access_token\":\"gho_abc\",\"token_type\":\"bearer\"}", MediaType.APPLICATION_JSON));

        assertThat(client.getAccessToken("code")).isEqualTo("gho_abc");
    }

    @Test
    void 만료되었거나_이미_사용한_code면_502가_아니라_400() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess("{\"error\":\"bad_verification_code\",\"error_description\":\"The code passed is incorrect or expired.\"}",
                        MediaType.APPLICATION_JSON));

        assertStatus(HttpStatus.BAD_REQUEST, () -> client.getAccessToken("used-code"));
    }

    @Test
    void client_설정이_잘못되면_502() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withSuccess("{\"error\":\"incorrect_client_credentials\"}", MediaType.APPLICATION_JSON));

        assertStatus(HttpStatus.BAD_GATEWAY, () -> client.getAccessToken("code"));
    }

    @Test
    void client_id나_secret이_비어_있으면_GitHub를_호출하지_않고_500() {
        ReflectionTestUtils.setField(client, "clientSecret", "");

        assertStatus(HttpStatus.INTERNAL_SERVER_ERROR, () -> client.getAccessToken("code"));
        server.verify();
    }

    @Test
    void GitHub_서버_오류면_502() {
        server.expect(requestTo(TOKEN_URL)).andRespond(withServerError());

        assertStatus(HttpStatus.BAD_GATEWAY, () -> client.getAccessToken("code"));
    }

    @Test
    void 사용자_정보를_조회한다() {
        server.expect(requestTo(USER_URL))
                .andRespond(withSuccess("{\"login\":\"octocat\",\"id\":1,\"avatar_url\":\"https://avatars/1\"}", MediaType.APPLICATION_JSON));

        GithubUserInfo userInfo = client.getUserInfo("gho_abc");

        assertThat(userInfo.login()).isEqualTo("octocat");
        assertThat(userInfo.avatarUrl()).isEqualTo("https://avatars/1");
    }

    @Test
    void 사용자_정보_조회가_거부되면_502() {
        server.expect(requestTo(USER_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertStatus(HttpStatus.BAD_GATEWAY, () -> client.getUserInfo("bad-token"));
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }
}
