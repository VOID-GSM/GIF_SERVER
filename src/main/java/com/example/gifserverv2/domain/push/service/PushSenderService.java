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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
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

    @Transactional
    public void sendNotification(Long targetUserId, String title, String body) {
        List<PushSubscription> subscriptions = pushSubscriptionRepository.findAllByUserId(targetUserId);

        if (subscriptions.isEmpty()) {
            log.info("유저 {}에 대한 활성화된 푸시 구독 정보가 없습니다.", targetUserId);
            return;
        }

        Map<String, String> payloadMap = Map.of(
                "title", title,
                "body", body
        );

        try {
            String payload = objectMapper.writeValueAsString(payloadMap);

            for (PushSubscription sub : subscriptions) {
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
        } catch (Exception e) {
            log.error("푸시 JSON 페이로드 직렬화 실패: {}", e.getMessage());
        }
    }
}
