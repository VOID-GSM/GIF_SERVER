package com.example.gifserverv2.domain.push.dto.response;

import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "알림")
public record GetNotificationResponse(
        @Schema(description = "알림 ID", example = "100")
        Long id,
        @Schema(description = "제목", example = "새 양식이 공지되었습니다")
        String title,
        @Schema(description = "내용", example = "1차 중간 보고서 양식이 공지되었습니다.")
        String body,
        @Schema(description = "눌렀을 때 이동할 프론트 경로", example = "/form/3")
        String targetUrl,
        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,
        @Schema(description = "받은 일시", example = "2026-11-09T10:00:00")
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