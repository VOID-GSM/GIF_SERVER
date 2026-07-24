package com.example.gifserverv2.domain.push.dto.response;

import com.example.gifserverv2.domain.push.entity.Notification;
import java.time.LocalDateTime;

public record GetNotificationResponse(
        Long id,
        String title,
        String body,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static GetNotificationResponse from(Notification notification) {
        return new GetNotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
