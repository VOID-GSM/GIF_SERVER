package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.service.AdditionalInfoService;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/additional-info")
@RequiredArgsConstructor
public class AdditionalInfoController {

    private final AdditionalInfoService additionalInfoService;
    private final AuthService authService;

    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> updateAdminInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AdminAdditionalInfoRequest request
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateAdminAdditionalInfo(user.userId(), request);

        String newAccessToken = authService.renewToken(user.userId());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/client")
    public ResponseEntity<Map<String, String>> updateClientAdditionalInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ClientAdditionalInfoRequest request
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateClientAdditionalInfo(user.userId(), request);

        String newAccessToken = authService.renewToken(user.userId());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}