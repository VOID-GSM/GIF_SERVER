package com.example.gifserverv2.domain.push.controller;

import com.example.gifserverv2.domain.push.dto.request.CreatePushSubscriptionRequest;
import com.example.gifserverv2.domain.push.dto.request.DeletePushSubscriptionRequest;
import com.example.gifserverv2.domain.push.service.PushSubscriptionService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "웹 푸시", description = "브라우저 Web Push 구독 관리. 외부 사용자를 포함해 로그인한 모든 사용자가 호출할 수 있다.")
@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final PushSubscriptionService pushSubscriptionService;

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    @Operation(summary = "VAPID 공개키 조회", description = "`PushManager.subscribe()`의 `applicationServerKey`로 쓸 공개키를 `{ \"publicKey\": \"...\" }` 형태로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    @Operation(summary = "푸시 구독 등록", description = "브라우저 `PushSubscription`의 endpoint와 키를 등록한다. 같은 endpoint가 이미 있으면 현재 사용자로 갱신한다. 요청의 `User-Agent`가 함께 저장된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "endpoint·p256dh·auth 누락")
    })
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreatePushSubscriptionRequest request, // 변경된 DTO 적용
            @Parameter(hidden = true) @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        pushSubscriptionService.subscribe(user.userId(), request, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "푸시 구독 해제", description = "내 구독 중 해당 endpoint를 삭제한다. 없어도 성공한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공"),
            @ApiResponse(responseCode = "400", description = "endpoint 누락")
    })
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody DeletePushSubscriptionRequest request
    ) {
        pushSubscriptionService.unsubscribe(user.userId(), request.getEndpoint());
        return ResponseEntity.noContent().build();
    }
}
