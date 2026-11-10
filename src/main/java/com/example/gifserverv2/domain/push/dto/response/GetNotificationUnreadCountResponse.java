package com.example.gifserverv2.domain.push.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안 읽은 알림 수")
public record GetNotificationUnreadCountResponse(
        @Schema(description = "안 읽은 알림 수", example = "3")
        long unreadCount
) {
    public static GetNotificationUnreadCountResponse from(long unreadCount) {
        return new GetNotificationUnreadCountResponse(unreadCount);
    }
}
