package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.auth.dto.request.GithubConnectRequest;
import com.example.gifserverv2.domain.auth.dto.request.UpdateCurrentUserRequest;
import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.coin.service.CoinSyncService;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.service.UserService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CoinSyncService coinSyncService;

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        UserEntity user = userService.requireUser(currentUser.userId());
        return userService.buildCurrentUserResponse(user);
    }

    @PatchMapping("/me")
    public CurrentUserResponse updateMe(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                        @Valid @RequestBody UpdateCurrentUserRequest request) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.updateCurrentUser(currentUser, request);
    }

    @PostMapping("/me/github")
    public CurrentUserResponse connectGithub(@AuthenticationPrincipal AuthenticatedUser currentUser,
                                             @Valid @RequestBody GithubConnectRequest request) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.connectGithub(currentUser, request.code());
    }

    @DeleteMapping("/me/github")
    public CurrentUserResponse disconnectGithub(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return userService.disconnectGithub(currentUser);
    }

    @PostMapping("/me/github/sync")
    public CurrentUserResponse syncGithubCoins(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        coinSyncService.syncCommitCoins(currentUser.userId());

        UserEntity user = userService.requireUser(currentUser.userId());
        return userService.buildCurrentUserResponse(user);
    }
}