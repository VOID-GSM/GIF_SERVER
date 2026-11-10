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

    // 일반 사용자 목록: 숨김·삭제된 댓글은 제외
    Page<ProjectComment> findByProjectIdAndHiddenFalseAndDeletedAtIsNullOrderByCreatedAtDescIdDesc(
            Long projectId, Pageable pageable);

    // 커서 목록: (createdAt, id) 가 커서 댓글보다 앞서는(더 오래된) 댓글을 최신순으로. 숨김·삭제 제외
    @Query("SELECT c FROM ProjectComment c WHERE c.projectId = :projectId " +
            "AND c.hidden = false AND c.deletedAt IS NULL " +
            "AND (c.createdAt < :createdAt OR (c.createdAt = :createdAt AND c.id < :id)) " +
            "ORDER BY c.createdAt DESC, c.id DESC")
    List<ProjectComment> findVisibleBefore(@Param("projectId") Long projectId,
                                           @Param("createdAt") Instant createdAt,
                                           @Param("id") Long id,
                                           Pageable pageable);

    // 기존 메서드는 유지한다. (기존 테스트 등이 쓸 수 있음)
    Page<ProjectComment> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId, Pageable pageable);
    long countByProjectId(Long projectId);

    // 화면에 보이는 댓글 수: 숨김·삭제 제외
    long countByProjectIdAndHiddenFalseAndDeletedAtIsNull(Long projectId);

    // 도배 방지는 삭제·숨김 댓글도 포함해서 센다. (지우고 다시 쓰는 방식으로 제한을 피하지 못하게)
    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);

    // 목록용: 프로젝트 id 묶음을 한 번에 집계 (N+1 방지), 숨김·삭제 제외. 댓글이 없는 프로젝트는 결과에 없다.
    @Query("SELECT c.projectId AS projectId, COUNT(c) AS commentCount FROM ProjectComment c " +
            "WHERE c.projectId IN :projectIds AND c.hidden = false AND c.deletedAt IS NULL " +
            "GROUP BY c.projectId")
    List<ProjectCommentCount> countByProjectIdIn(@Param("projectIds") Collection<Long> projectIds);

    interface ProjectCommentCount {
        Long getProjectId();
        long getCommentCount();
    }

    // 관리자 목록: 숨김 댓글 포함, 삭제된 댓글 제외. 프로젝트·작성자 필터는 값이 없으면 무시한다.
    @Query("SELECT c FROM ProjectComment c WHERE c.deletedAt IS NULL " +
            "AND (:projectId IS NULL OR c.projectId = :projectId) " +
            "AND (:userId IS NULL OR c.userId = :userId)")
    Page<ProjectComment> findForAdmin(@Param("projectId") Long projectId,
                                      @Param("userId") Long userId,
                                      Pageable pageable);
}