package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import com.example.gifserverv2.domain.push.entity.PushOptions;
import com.example.gifserverv2.domain.push.entity.PushSubscription;
import com.example.gifserverv2.domain.push.repository.NotificationRepository;
import com.example.gifserverv2.domain.push.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushSenderService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    private final PushService pushService;

    @Async
    public void sendNotification(Long targetUserId, String title, String body, String targetUrl, String tag) {
        saveNotificationHistory(targetUserId, title, body, targetUrl);

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserId(targetUserId);
        if (subscriptions.isEmpty()) {
            log.info("유저 {}에 대한 활성화된 푸시 구독 정보가 없습니다.", targetUserId);
            return;
        }

        String payload = createPayload(title, body, targetUrl, tag);
        if (payload == null) return;

        for (PushSubscription sub : subscriptions) {
            sendToSubscription(sub, payload, PushOptions.DEFAULT);
        }
    }

    public void sendBulkNotifications(List<Long> userIds, String title, String body, String targetUrl, String tag) {
        sendBulkNotifications(userIds, title, body, targetUrl, tag, PushOptions.DEFAULT);
    }

    public void sendBulkNotifications(List<Long> userIds, String title, String body, String targetUrl, String tag,
                                      PushOptions options) {
        if (userIds == null || userIds.isEmpty()) return;

        saveBulkNotificationHistory(userIds, title, body, targetUrl);

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserIdIn(userIds);
        if (subscriptions.isEmpty()) {
            log.info("대상 유저들에 대한 활성화된 푸시 구독 정보가 없습니다.");
            return;
        }

        String payload = createPayload(title, body, targetUrl, tag);
        if (payload == null) return;

        for (PushSubscription subscription : subscriptions) {
            sendToSubscription(subscription, payload, options);
        }
    }

    @Async
    public void sendBulkNotificationsAsync(List<Long> userIds, String title, String body, String targetUrl,
                                           String tag) {
        sendBulkNotifications(userIds, title, body, targetUrl, tag);
    }

    @Transactional
    public void saveNotificationHistory(Long targetUserId, String title, String body, String targetUrl) {
        notificationRepository.save(NotificationHistory.builder()
                .userId(targetUserId)
                .title(title)
                .body(body)
                .targetUrl(targetUrl)
                .build());
    }

    @Transactional
    public void saveBulkNotificationHistory(List<Long> userIds, String title, String body, String targetUrl) {
        List<NotificationHistory> notifications = userIds.stream()
                .map(userId -> NotificationHistory.builder()
                        .userId(userId)
                        .title(title)
                        .body(body)
                        .targetUrl(targetUrl)
                        .build())
                .toList();
        notificationRepository.saveAll(notifications);
    }

    private void sendToSubscription(PushSubscription sub, String payload, PushOptions options) {
        try {
            Notification.NotificationBuilder builder = Notification.builder()
                    .endpoint(sub.getEndpoint())
                    .userPublicKey(sub.getP256dh())
                    .userAuth(sub.getAuth())
                    .payload(payload)
                    .ttl(options.ttlSeconds());
            if (options.urgency() != null) {
                builder.urgency(nl.martijndwars.webpush.Urgency.valueOf(options.urgency().name()));
            }
            if (options.topic() != null) {
                builder.topic(options.topic());
            }
            Notification notification = builder.build();

            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 410 || statusCode == 404) {
                log.warn("만료된 푸시 엔드포인트 발견되어 삭제 처리합니다: {}", sub.getEndpoint());
                deleteSubscription(sub);
            }

        } catch (Exception e) {
            log.error("특정 기기 푸시 발송 실패 (Endpoint: {}): {}", sub.getEndpoint(), e.getMessage());
        }
    }

    @Transactional
    public void deleteSubscription(PushSubscription sub) {
        pushSubscriptionRepository.delete(sub);
    }

    // tag 는 받는 사람마다 따로 생기는 알림 id 가 아니라 "알림 종류 + 대상 id" 라서 일괄 발송에서도 payload 하나를 공유할 수 있다.
    String createPayload(String title, String body, String targetUrl, String tag) {
        try {
            Map<String, String> payloadMap = new HashMap<>();
            payloadMap.put("title", title);
            payloadMap.put("body", body);
            if (targetUrl != null) {
                payloadMap.put("targetUrl", targetUrl);
                payloadMap.put("url", targetUrl);
            }
            if (tag != null) {
                payloadMap.put("tag", tag);
            }
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("푸시 JSON 페이로드 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}