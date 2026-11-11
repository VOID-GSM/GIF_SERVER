package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface ProjectLikeRepository extends JpaRepository<ProjectLike, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    @Query("SELECT l.projectId AS projectId, COUNT(l) AS likeCount FROM ProjectLike l " +
            "WHERE l.projectId IN :projectIds GROUP BY l.projectId")
    List<ProjectLikeCount> countByProjectIdIn(@Param("projectIds") Collection<Long> projectIds);

    @Query("SELECT l.projectId FROM ProjectLike l WHERE l.userId = :userId AND l.projectId IN :projectIds")
    List<Long> findLikedProjectIds(@Param("userId") Long userId, @Param("projectIds") Collection<Long> projectIds);

    // 마이페이지 좋아요한 프로젝트: 좋아요 누른 최신순. 없는 프로젝트는 제외한다.
    @Query(value = "SELECT l FROM ProjectLike l WHERE l.userId = :userId " +
            "AND EXISTS (SELECT 1 FROM Project p WHERE p.id = l.projectId) " +
            "ORDER BY l.createdAt DESC, l.id DESC",
            countQuery = "SELECT COUNT(l) FROM ProjectLike l WHERE l.userId = :userId " +
                    "AND EXISTS (SELECT 1 FROM Project p WHERE p.id = l.projectId)")
    Page<ProjectLike> findMine(@Param("userId") Long userId, Pageable pageable);

    interface ProjectLikeCount {
        Long getProjectId();
        long getLikeCount();
    }

    @Transactional
    @Modifying
    @Query("DELETE FROM ProjectLike l WHERE l.projectId = :projectId AND l.userId = :userId")
    int deleteByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
