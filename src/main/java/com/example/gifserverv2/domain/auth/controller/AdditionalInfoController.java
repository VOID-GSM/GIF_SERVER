package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.service.AdditionalInfoService;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders; // 임포트 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie; // 임포트 추가
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/additional-info")
@RequiredArgsConstructor
public class AdditionalInfoController {

    private final AdditionalInfoService additionalInfoService;
    private final AuthService authService;

    @PostMapping("/admin")
    public ResponseEntity<Void> updateAdminInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AdminAdditionalInfoRequest request, // @Valid 추가
            HttpServletResponse response
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateAdminAdditionalInfo(user.userId(), request);

        String newAccessToken = authService.renewToken(user.userId());

        ResponseCookie cookie = ResponseCookie.from("access_token", newAccessToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(3600)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/client")
    public ResponseEntity<Void> updateClientAdditionalInfo(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ClientAdditionalInfoRequest request
    ) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateClientAdditionalInfo(currentUser.userId(), request);
        return ResponseEntity.noContent().build();
    }
}