package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProjectLikeRepository extends JpaRepository<ProjectLike, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    /** 삭제된 행 수(0 또는 1)를 반환한다. */
    @Transactional
    @Modifying
    @Query("DELETE FROM ProjectLike l WHERE l.projectId = :projectId AND l.userId = :userId")
    int deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
