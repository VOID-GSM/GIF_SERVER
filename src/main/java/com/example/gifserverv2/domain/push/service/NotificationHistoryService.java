package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.dto.response.GetNotificationResponse;
import com.example.gifserverv2.domain.push.dto.response.GetNotificationUnreadCountResponse;
import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import com.example.gifserverv2.domain.push.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryService {

    private final NotificationRepository notificationRepository;

    public Slice<GetNotificationResponse> getNotifications(Long userId, Integer days, Pageable pageable) {
        int targetDays = (days == null || days <= 0) ? 7 : days;
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(targetDays);

        return notificationRepository
                .findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(userId, startDateTime, pageable)
                .map(GetNotificationResponse::from);
    }

    public GetNotificationUnreadCountResponse getNotificationUnreadCount(Long userId, Integer days) {
        int targetDays = (days == null || days <= 0) ? 7 : days;
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(targetDays);

        long count = notificationRepository.countByUserIdAndIsReadFalseAndCreatedAtGreaterThanEqual(userId, startDateTime);
        return GetNotificationUnreadCountResponse.from(count);
    }

    @Transactional
    public void patchNotificationRead(Long userId, Long notificationId) {
        NotificationHistory notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.markAsRead();
    }

    @Transactional
    public void patchAllNotificationsRead(Long userId, Integer days) {
        int targetDays = (days == null || days <= 0) ? 7 : days;
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(targetDays);

        notificationRepository.markAllAsReadByUserIdAndCreatedAtGreaterThanEqual(userId, startDateTime);
    }
}