package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    Page<ProjectComment> findByProjectIdOrderByCreatedAtDescIdDesc(Long projectId, Pageable pageable);
    long countByProjectId(Long projectId);
    long countByUserIdAndCreatedAtAfter(Long userId, Instant since);
}
