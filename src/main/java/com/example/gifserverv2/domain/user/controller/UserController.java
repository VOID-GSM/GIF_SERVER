package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.auth.dto.request.GithubConnectRequest;
import com.example.gifserverv2.domain.auth.dto.request.UpdateCurrentUserRequest;
import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.coin.service.CoinSyncService;
import com.example.gifserverv2.domain.user.dto.response.GithubSyncResponse;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.service.UserService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "내 정보", description = "로그인한 사용자 정보 조회·수정과 GitHub 연동, 커밋 코인 동기화. 외부 사용자도 `GET/PATCH /api/users/me`는 호출할 수 있다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CoinSyncService coinSyncService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자(외부 사용자 포함)의 정보와 소속 프로젝트, 코인 잔액을 반환한다. 이 응답의 `accessToken`은 null이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        UserEntity user = userService.requireUser(currentUser.userId());
        return userService.buildCurrentUserResponse(user);
    }

    @Operation(summary = "내 정보 수정", description = """
            보낸 값만 바뀌고 생략한(null·빈 문자열) 값은 유지된다. 응답의 `accessToken`에 수정 내용이 반영된 새 토큰이 담기므로 교체해야 한다.
            - `adminRole`, `adminTeam`: 선생님만 수정할 수 있다.
            - `clientRole`: 학생만 수정할 수 있다.
            - 외부 사용자는 `studentNumber`, `clientRole`을 보낼 수 없다(403 `EXTERNAL_USER_FORBIDDEN`).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공. 새 accessToken 포함"),
            @ApiResponse(responseCode = "400", description = "학번이 숫자가 아님, 사용할 수 없는 adminRole"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "학생이 선생님 필드 수정, 선생님이 학생 필드 수정, 외부 사용자가 학생 정보 수정")
    })
    @PatchMapping("/me")
    public CurrentUserResponse updateMe(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                        @Valid @RequestBody UpdateCurrentUserRequest request) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.updateCurrentUser(currentUser, request);
    }

    @Operation(summary = "GitHub 계정 연동", description = "GitHub OAuth로 받은 인가 코드로 계정을 연동한다. 연동한 시각 이후에 푸시한 커밋만 코인으로 적립된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "연동 성공"),
            @ApiResponse(responseCode = "400", description = "잘못되었거나 만료된 code"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 다른 계정에 연동된 GitHub 계정"),
            @ApiResponse(responseCode = "500", description = "서버에 GitHub 연동 설정이 없음"),
            @ApiResponse(responseCode = "502", description = "GitHub 서버와 통신 실패")
    })
    @PostMapping("/me/github")
    public CurrentUserResponse connectGithub(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                             @Valid @RequestBody GithubConnectRequest request) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.connectGithub(currentUser, request.code());
    }

    @Operation(summary = "GitHub 연동 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "해제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @DeleteMapping("/me/github")
    public CurrentUserResponse disconnectGithub(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.disconnectGithub(currentUser);
    }

    @Operation(summary = "GitHub 커밋 코인 동기화", description = """
            GitHub 연동 이후 **모든 브랜치**에 푸시한 내 커밋 중 아직 지급하지 않은 커밋 1개당 10코인을 적립한다.
            내 정보 필드에 이번에 얻은 코인(`earnedCoin`)이 더해진 형태로 응답한다. 새 커밋이 없으면 `earnedCoin`은 0이다.

            - 같은 커밋은 여러 브랜치에 푸시하거나 리베이스·다시 동기화해도 한 번만 지급한다. 머지 커밋은 지급하지 않는다.
            - GitHub 계정과 연결된 이메일로 만든 내 커밋만 집계한다. 다른 사람·포크 저장소에 푸시한 내 커밋도 포함한다.
            - GitHub Events API 기준이라 푸시가 반영되기까지 몇 분~몇 시간 걸릴 수 있다. 최근 90일·이벤트 300개까지만 조회되므로 오래 동기화하지 않으면 일부 커밋이 빠질 수 있다.
            - GitHub API 호출에 실패하면 0코인이 아니라 502 를 반환한다. (토큰 만료 시 다시 연동 안내)""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "동기화 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "500", description = "GitHub 계정이 연동되지 않음"),
            @ApiResponse(responseCode = "502", description = "GitHub API 호출 실패 (토큰 만료, 호출 한도 초과, 통신 실패)")
    })
    @PostMapping("/me/github/sync")
    public GithubSyncResponse syncGithubCoins(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        int earnedCoin = coinSyncService.syncCommitCoins(currentUser.userId());

        UserEntity user = userService.requireUser(currentUser.userId());
        return new GithubSyncResponse(userService.buildCurrentUserResponse(user), earnedCoin);
    }
}