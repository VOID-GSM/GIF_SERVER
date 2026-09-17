package com.example.gifserverv2.domain.retrospective.repository;

import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RetrospectiveBookmarkRepository extends JpaRepository<RetrospectiveBookmark, Long> {
    boolean existsByRetrospectiveIdAndUserId(Long retrospectiveId, Long userId);
    Optional<RetrospectiveBookmark> findByRetrospectiveIdAndUserId(Long retrospectiveId, Long userId);
    List<RetrospectiveBookmark> findAllByUserIdAndRetrospectiveIdIn(Long userId, List<Long> retrospectiveIds);
    void deleteAllByRetrospectiveId(Long retrospectiveId);
}
