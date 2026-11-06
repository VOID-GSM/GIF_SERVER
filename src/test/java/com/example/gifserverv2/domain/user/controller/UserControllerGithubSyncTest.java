package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.coin.service.CoinSyncService;
import com.example.gifserverv2.domain.user.dto.response.GithubSyncResponse;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.service.UserService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserControllerGithubSyncTest {

    @Mock UserService userService;
    @Mock CoinSyncService coinSyncService;
    @InjectMocks UserController controller;

    private final AuthenticatedUser currentUser =
            new AuthenticatedUser(1L, "student@gsm.hs.kr", "홍길동", "2101", Role.USER, null, null, false, null);

    @Test
    void 동기화_응답은_사용자_정보와_이번에_얻은_코인을_펼쳐서_내려준다() throws Exception {
        UserEntity user = new UserEntity("student@gsm.hs.kr", "홍길동", "2101", Role.USER);
        given(coinSyncService.syncCommitCoins(1L)).willReturn(30);
        given(userService.requireUser(1L)).willReturn(user);
        given(userService.buildCurrentUserResponse(user)).willReturn(currentUserResponse(230));

        GithubSyncResponse response = controller.syncGithubCoins(currentUser);
        JsonNode json = new ObjectMapper().valueToTree(response);

        assertThat(response.earnedCoin()).isEqualTo(30);
        assertThat(json.get("earnedCoin").asInt()).isEqualTo(30);
        assertThat(json.get("coinBalance").asInt()).isEqualTo(230);
        assertThat(json.get("id").asLong()).isEqualTo(1L);
        assertThat(json.get("githubUsername").asText()).isEqualTo("octocat");
        assertThat(json.has("user")).isFalse();
    }

    @Test
    void 새_커밋이_없으면_earnedCoin은_0이다() throws Exception {
        UserEntity user = new UserEntity("student@gsm.hs.kr", "홍길동", "2101", Role.USER);
        given(coinSyncService.syncCommitCoins(1L)).willReturn(0);
        given(userService.requireUser(1L)).willReturn(user);
        given(userService.buildCurrentUserResponse(user)).willReturn(currentUserResponse(200));

        JsonNode json = new ObjectMapper().valueToTree(controller.syncGithubCoins(currentUser));

        assertThat(json.get("earnedCoin").asInt()).isZero();
        assertThat(json.get("coinBalance").asInt()).isEqualTo(200);
    }

    private static CurrentUserResponse currentUserResponse(int coinBalance) {
        return new CurrentUserResponse(1L, "student@gsm.hs.kr", "홍길동", "2101", "2", "USER", "STUDENT",
                null, null, false, null, null, null, "octocat", null, coinBalance, null);
    }
}
