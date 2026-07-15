package com.example.gifserverv2.domain.push.controller;

import com.example.gifserverv2.domain.push.dto.request.CreatePushSubscriptionRequest;
import com.example.gifserverv2.domain.push.dto.request.DeletePushSubscriptionRequest;
import com.example.gifserverv2.domain.push.service.PushSubscriptionService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePushSubscriptionRequest request, // 변경된 DTO 적용
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        pushSubscriptionService.subscribe(user.userId(), request, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody DeletePushSubscriptionRequest request
    ) {
        pushSubscriptionService.unsubscribe(user.userId(), request.getEndpoint());
        return ResponseEntity.noContent().build();
    }
}
