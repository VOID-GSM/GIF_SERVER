package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.domain.auth.dto.response.CurrentUserResponse;
import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.domain.auth.service.DgOAuthFlowService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DgOAuthFlowService dgOAuthFlowService;

    @GetMapping("/dg/start")
    public ResponseEntity<Void> startDgLogin(@RequestParam String redirectUri) {
        URI location = dgOAuthFlowService.createLoginRedirect(redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/dg/callback")
    public OAuthSignInResponse dgCallback(@RequestParam String code, @RequestParam String state) {
        return dgOAuthFlowService.completeLogin(code, state);
    }

    @PostMapping("/signin")
    public OAuthSignInResponse signIn(@Valid @RequestBody OAuthSignInRequest request) {
        return authService.signIn(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        return new CurrentUserResponse(
                currentUser.userId(),
                currentUser.email(),
                currentUser.name(),
                currentUser.studentNumber(),
                currentUser.role().name());
    }
}
