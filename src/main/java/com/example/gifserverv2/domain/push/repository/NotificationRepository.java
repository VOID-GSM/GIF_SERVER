package com.example.gifserverv2.domain.push.repository;

import com.example.gifserverv2.domain.push.entity.NotificationHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<NotificationHistory, Long> {

    Slice<NotificationHistory> findAllByUserIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDateTime, Pageable pageable);

    long countByUserIdAndIsReadFalseAndCreatedAtGreaterThanEqual(
            Long userId, LocalDateTime startDateTime);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE NotificationHistory n SET n.isRead = true " +
            "WHERE n.userId = :userId " +
            "AND n.isRead = false " +
            "AND n.createdAt >= :startDateTime")
    int markAllAsReadByUserIdAndCreatedAtGreaterThanEqual(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime);
}