package com.example.gifserverv2.domain.push.repository;

import com.example.gifserverv2.domain.push.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpoint(String endpoint);

    void deleteByUserIdAndEndpoint(Long userId, String endpoint);

    List<PushSubscription> findAllByUserId(Long userId);

    List<PushSubscription> findAllByUserIdIn(List<Long> userIds);
}