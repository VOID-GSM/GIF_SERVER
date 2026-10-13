package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public interface ProjectViewRepository extends JpaRepository<ProjectView, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Transactional
    @Modifying
    @Query("""
            UPDATE ProjectView v SET v.lastViewedAt = :now
            WHERE v.projectId = :projectId AND v.userId = :userId AND v.lastViewedAt < :cutoff
            """)
    int refreshIfExpired(
            @Param("projectId") Long projectId,
            @Param("userId") Long userId,
            @Param("now") Instant now,
            @Param("cutoff") Instant cutoff
    );
}
