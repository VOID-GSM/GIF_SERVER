package com.example.gifserverv2.domain.user.dto.response;

import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * GitHub 커밋 동기화 응답. CurrentUserResponse 필드를 그대로 펼쳐서 내려주고, 이번 동기화로 얻은 코인(earnedCoin)을 더한다.
 * 새로 반영된 커밋이 없으면 earnedCoin 은 0 이다.
 */
public record GithubSyncResponse(
        @JsonUnwrapped CurrentUserResponse user,
        int earnedCoin
) {
}
