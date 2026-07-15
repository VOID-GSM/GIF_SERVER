package com.example.gifserverv2.domain.push.repository;

import com.example.gifserverv2.domain.push.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    Optional<PushSubscription> findByEndpoint(String endpoint);

    void deleteByUserIdAndEndpoint(Long userId, String endpoint);
}