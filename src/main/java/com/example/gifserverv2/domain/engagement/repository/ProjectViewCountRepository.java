package com.example.gifserverv2.domain.engagement.repository;

import com.example.gifserverv2.domain.engagement.entity.ProjectViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectViewCountRepository extends JpaRepository<ProjectViewCount, Long> {

    @Modifying
    @Query(value = "INSERT INTO project_view_count (project_id, views) VALUES (:projectId, 1) "
            + "ON DUPLICATE KEY UPDATE views = views + 1", nativeQuery = true)
    void increase(@Param("projectId") Long projectId);
}