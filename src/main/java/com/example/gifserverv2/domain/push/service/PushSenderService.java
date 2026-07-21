package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.entity.PushSubscription;
import com.example.gifserverv2.domain.push.repository.PushSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushSenderService {

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${vapid.public-key}")
    private String publicKey;

    @Value("${vapid.private-key}")
    private String privateKey;

    @Value("${vapid.subject}")
    private String subject;

    private PushService pushService;

    @PostConstruct
    public void init() throws GeneralSecurityException {
        this.pushService = new PushService(publicKey, privateKey, subject);
    }

    @Async
    public void sendNotification(Long targetUserId, String title, String body) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserId(targetUserId);

        if (subscriptions.isEmpty()) {
            log.info("유저 {}에 대한 활성화된 푸시 구독 정보가 없습니다.", targetUserId);
            return;
        }

        String payload = createPayload(title, body);
        if (payload == null) return;

        for (PushSubscription sub : subscriptions) {
            sendToSubscription(sub, payload);
        }
    }

    public void sendBulkNotifications(List<Long> userIds, String title, String body) {
        if (userIds == null || userIds.isEmpty()) return;

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserIdIn(userIds);

        if (subscriptions.isEmpty()) {
            log.info("대상 유저들에 대한 활성화된 푸시 구독 정보가 없습니다.");
            return;
        }

        String payload = createPayload(title, body);
        if (payload == null) return;

        for (PushSubscription subscription : subscriptions) {
            sendToSubscription(subscription, payload);
        }
    }

    private void sendToSubscription(PushSubscription sub, String payload) {
        try {
            Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payload
            );

            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 410 || statusCode == 404) {
                log.warn("만료된 푸시 엔드포인트 발견되어 삭제 처리합니다: {}", sub.getEndpoint());
                pushSubscriptionRepository.delete(sub);
            }

        } catch (Exception e) {
            log.error("특정 기기 푸시 발송 실패 (Endpoint: {}): {}", sub.getEndpoint(), e.getMessage());
        }
    }

    private String createPayload(String title, String body) {
        try {
            Map<String, String> payloadMap = Map.of(
                    "title", title,
                    "body", body
            );
            return objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("푸시 JSON 페이로드 직렬화 실패: {}", e.getMessage());
            return null;
        }
    }
}