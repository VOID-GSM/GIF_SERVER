package com.example.gifserverv2.domain.push.controller;

import com.example.gifserverv2.domain.push.dto.response.GetNotificationResponse;
import com.example.gifserverv2.domain.push.dto.response.GetNotificationUnreadCountResponse;
import com.example.gifserverv2.domain.push.service.NotificationHistoryService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationHistoryService notificationHistoryService;

    @GetMapping
    public ResponseEntity<Slice<GetNotificationResponse>> getNotifications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(notificationHistoryService.getNotifications(user.userId(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<GetNotificationUnreadCountResponse> getNotificationUnreadCount(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(notificationHistoryService.getNotificationUnreadCount(user.userId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> patchNotificationRead(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id
    ) {
        notificationHistoryService.patchNotificationRead(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
