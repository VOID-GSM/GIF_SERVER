package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ScoreSupport {

    private final ScoreRepository scoreRepository;
    private final ProjectRepository projectRepository;

    public ScoreSupport(ScoreRepository scoreRepository, ProjectRepository projectRepository) {
        this.scoreRepository = scoreRepository;
        this.projectRepository = projectRepository;
    }

    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));
    }

    public Score getScoreOrThrow(Project project, String evaluatorId) {
        return scoreRepository.findByProjectAndEvaluatorId(project, evaluatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 평가자의 점수를 찾을 수 없습니다."));
    }

    public void validateCommonRequest(Long projectId, String evaluatorId) {
        if (projectId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectId는 필수입니다.");
        }
        if (evaluatorId == null || evaluatorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "evaluatorId는 필수입니다.");
        }
    }

    public void requireScore(Integer score, String fieldName) {
        if (score == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "는 필수입니다.");
        }
    }

    public ScoreRepository scoreRepository() { return scoreRepository; }
}

