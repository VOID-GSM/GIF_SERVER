package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    Page<ProjectComment> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId, Pageable pageable);
    long countByProjectId(Long projectId);
    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);

    // 목록용: 프로젝트마다 따로 세지 않고 한 번에 센다 (N+1 방지). 댓글이 없는 프로젝트는 결과에 없다.
    @Query("SELECT c.projectId AS projectId, COUNT(c) AS commentCount FROM ProjectComment c " +
            "WHERE c.projectId IN :projectIds GROUP BY c.projectId")
    List<ProjectCommentCount> countByProjectIdIn(@Param("projectIds") Collection<Long> projectIds);

    interface ProjectCommentCount {
        Long getProjectId();
        long getCommentCount();
    }
}