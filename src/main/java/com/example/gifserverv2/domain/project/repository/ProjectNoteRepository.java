package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.ProjectNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectNoteRepository extends JpaRepository<ProjectNote, Long> {
    Optional<ProjectNote> findByProjectIdAndUserId(Long projectId, Long userId);
}