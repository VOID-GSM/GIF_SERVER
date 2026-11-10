package com.example.gifserverv2.domain.push.controller;

import com.example.gifserverv2.domain.push.dto.response.GetNotificationResponse;
import com.example.gifserverv2.domain.push.dto.response.GetNotificationUnreadCountResponse;
import com.example.gifserverv2.domain.push.service.NotificationHistoryService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 내역", description = "앱 내 알림 내역 조회와 읽음 처리. 외부 사용자를 포함해 로그인한 모든 사용자가 호출할 수 있다.")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationHistoryService notificationHistoryService;

    @Operation(summary = "알림 목록", description = "최근 `days`일 동안 받은 알림을 최신순으로 반환한다. `page`(0부터), `size`(기본 20)로 페이지를 지정하며 응답은 Slice 형태(`content`, `last` 등)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Slice<GetNotificationResponse>> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "최근 며칠 치 알림을 대상으로 할지 (기본 7, 0 이하이면 7)", example = "7") @RequestParam(name = "days", required = false, defaultValue = "7") Integer days,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationHistoryService.getNotifications(user.userId(), days, pageable));
    }

    @Operation(summary = "안 읽은 알림 수")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/unread-count")
    public ResponseEntity<GetNotificationUnreadCountResponse> getNotificationUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "최근 며칠 치 알림을 대상으로 할지 (기본 7, 0 이하이면 7)", example = "7") @RequestParam(name = "days", required = false, defaultValue = "7") Integer days
    ) {
        return ResponseEntity.ok(notificationHistoryService.getNotificationUnreadCount(user.userId(), days));
    }

    @Operation(summary = "알림 읽음 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "알림이 없거나 내 알림이 아님")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> patchNotificationRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "알림 ID", example = "100") @PathVariable Long id
    ) {
        notificationHistoryService.patchNotificationRead(user.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 모두 읽음 처리", description = "최근 `days`일 동안 받은 알림을 모두 읽음 처리한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "처리 성공")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<Void> patchAllNotificationsRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "최근 며칠 치 알림을 대상으로 할지 (기본 7, 0 이하이면 7)", example = "7") @RequestParam(name = "days", required = false, defaultValue = "7") Integer days
    ) {
        notificationHistoryService.patchAllNotificationsRead(user.userId(), days);
        return ResponseEntity.noContent().build();
    }
}