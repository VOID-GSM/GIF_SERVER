package com.example.gifserverv2.domain.push.repository;

import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationHistory, Long> {

    Slice<NotificationHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);
}
