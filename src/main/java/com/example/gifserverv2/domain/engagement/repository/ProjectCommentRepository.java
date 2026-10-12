package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCommentRepository extends JpaRepository<ProjectComment, Long> {
    Page<ProjectComment> findByProjectIdOrderByCreatedAtDesc(Long projectId, Pageable pageable);
    long countByProjectId(Long projectId);
}