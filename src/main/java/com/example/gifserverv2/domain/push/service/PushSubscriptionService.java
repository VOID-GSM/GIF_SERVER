package com.example.gifserverv2.domain.push.service;

import com.example.gifserverv2.domain.push.dto.request.CreatePushSubscriptionRequest;
import com.example.gifserverv2.domain.push.entity.PushSubscription;
import com.example.gifserverv2.domain.push.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Transactional
    public void subscribe(Long userId, CreatePushSubscriptionRequest request, String userAgent) {
        pushSubscriptionRepository.findByEndpoint(request.getEndpoint())
                .ifPresentOrElse(
                        existing -> existing.update(userId, request.getP256dh(), request.getAuth(), userAgent),
                        () -> pushSubscriptionRepository.save(PushSubscription.builder()
                                .userId(userId)
                                .endpoint(request.getEndpoint())
                                .p256dh(request.getP256dh())
                                .auth(request.getAuth())
                                .userAgent(userAgent)
                                .build())
                );
    }

    @Transactional
    public void unsubscribe(Long userId, String endpoint) {
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
    }
}
