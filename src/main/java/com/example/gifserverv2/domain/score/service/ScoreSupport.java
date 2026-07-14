package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public Score getScoreOrNull(Project project, String evaluatorId) {
        try {
            return getScoreOrThrow(project, evaluatorId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    public void validateCommonRequest(Long projectId, String evaluatorId) {
        if (projectId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "projectId는 필수입니다.");
        }
        if (evaluatorId == null || evaluatorId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "evaluatorId는 필수입니다.");
        }
    }

    public void requireEvaluatorId(AuthenticatedUser evaluator) {
        if (evaluator == null || evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }
    }

    public void validateEvaluator(AuthenticatedUser evaluator, Predicate<AuthenticatedUser> roleAllowed, String forbiddenMessage) {
        if (evaluator == null || !roleAllowed.test(evaluator)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        requireEvaluatorId(evaluator);
    }

    public void requireScore(Integer score, String fieldName) {
        if (score == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + "는 필수입니다.");
        }
    }

    public void requireScores(Map<String, Integer> scores) {
        scores.forEach((fieldName, score) -> requireScore(score, fieldName));
    }

    public Score upsertScore(Project project, String evaluatorId, Supplier<Score> createSupplier, Consumer<Score> updateConsumer) {
        return scoreRepository.findByProjectAndEvaluatorId(project, evaluatorId)
                .map(score -> {
                    updateConsumer.accept(score);
                    return score;
                })
                .orElseGet(() -> scoreRepository.save(createSupplier.get()));
    }
}

