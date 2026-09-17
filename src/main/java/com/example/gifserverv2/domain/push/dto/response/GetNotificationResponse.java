package com.example.gifserverv2.domain.push.dto.response;

import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import java.time.LocalDateTime;

public record GetNotificationResponse(
        Long id,
        String title,
        String body,
        String targetUrl,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static GetNotificationResponse from(NotificationHistory notification) {
        return new GetNotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}