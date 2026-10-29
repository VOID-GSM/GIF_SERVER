package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.domain.auth.service.DgOAuthFlowService;
import com.example.gifserverv2.domain.auth.service.GoogleOAuthFlowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DgOAuthFlowService dgOAuthFlowService;
    private final GoogleOAuthFlowService googleOAuthFlowService;

    @GetMapping("/dg/start")
    public ResponseEntity<Void> startDgLogin(@RequestParam String redirectUri) {
        URI location = dgOAuthFlowService.createLoginRedirect(redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/dg/callback")
    public OAuthSignInResponse dgCallback(@RequestParam String code, @RequestParam String state) {
        return dgOAuthFlowService.completeLogin(code, state);
    }

    @GetMapping("/google/start")
    public ResponseEntity<Void> startGoogleLogin(@RequestParam String redirectUri) {
        URI location = googleOAuthFlowService.createLoginRedirect(redirectUri);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/google/callback")
    public OAuthSignInResponse googleCallback(@RequestParam String code, @RequestParam String state) {
        return googleOAuthFlowService.completeLogin(code, state);
    }

    @PostMapping("/signin")
    public OAuthSignInResponse signIn(@Valid @RequestBody OAuthSignInRequest request) {
        return authService.signIn(request);
    }
}