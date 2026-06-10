package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.service.AdditionalInfoService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/additional-info")
@RequiredArgsConstructor
public class AdditionalInfoController {

    private final AdditionalInfoService additionalInfoService;

    @PostMapping("/admin")
    public ResponseEntity<Void> updateAdminAdditionalInfo(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AdminAdditionalInfoRequest request
    ) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateAdminAdditionalInfo(currentUser.userId(), request);
        return ResponseEntity.noContent().build();
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
