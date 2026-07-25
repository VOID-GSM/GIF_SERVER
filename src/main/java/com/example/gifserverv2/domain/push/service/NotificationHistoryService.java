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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationHistoryService {

    private final NotificationRepository notificationRepository;

    public Slice<GetNotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(GetNotificationResponse::from);
    }

    public GetNotificationUnreadCountResponse getNotificationUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
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
}
