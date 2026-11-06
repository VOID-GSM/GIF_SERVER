package com.example.gifserverv2.domain.score.repository;

import com.example.gifserverv2.domain.score.entity.ProjectItemScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectItemScoreRepository extends JpaRepository<ProjectItemScore, Long> {

    List<ProjectItemScore> findByProjectIdAndEvaluatorId(Long projectId, Long evaluatorId);
}
