package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.ProjectLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectLinkRepository extends JpaRepository<ProjectLink, Long> {

    List<ProjectLink> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);
}