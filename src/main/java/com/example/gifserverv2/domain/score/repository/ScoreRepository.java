package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.score.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByProjectAndEvaluatorId(Project project, String evaluatorId);

    boolean existsByProjectAndEvaluatorId(Project project, String evaluatorId);

    java.util.List<Score> findByProject(Project project);

    @Query("SELECT s FROM Score s WHERE s.project.id IN :projectIds")
    List<Score> findByProjectIds(@Param("projectIds") List<Long> projectIds);
}