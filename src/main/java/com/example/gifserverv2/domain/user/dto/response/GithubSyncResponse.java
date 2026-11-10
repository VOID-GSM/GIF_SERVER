package com.example.gifserverv2.domain.user.dto.response;

import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * GitHub 커밋 동기화 응답. CurrentUserResponse 필드를 그대로 펼쳐서 내려주고, 이번 동기화로 얻은 코인(earnedCoin)을 더한다.
 * 새로 반영된 커밋이 없으면 earnedCoin 은 0 이다.
 */
@Schema(description = "GitHub 커밋 동기화 결과. 내 정보 필드가 그대로 펼쳐지고 earnedCoin이 추가된다")
public record GithubSyncResponse(
        @JsonUnwrapped CurrentUserResponse user,
        @Schema(description = "이번 동기화로 얻은 코인. 새 커밋이 없으면 0", example = "30")
        int earnedCoin
) {
}
