package com.example.gifserverv2.domain.push.dto.response;

public record GetNotificationUnreadCountResponse(
        long unreadCount
) {
    public static GetNotificationUnreadCountResponse from(long unreadCount) {
        return new GetNotificationUnreadCountResponse(unreadCount);
    }
}
