package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateMajorScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchMajorScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.user.entity.AdminRole;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MajorScoreService {

    private final ScoreSupport support;

    @Transactional
    public void createMajor(CreateMajorScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, request.getProjectId(),
                request.getTechnicalCompleteness(), request.getSocialValueMajor(),
                request.getAiUtilityMajorScore(), request.getPresentationMajor());

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluator.userId().toString().trim();

        support.upsertScore(
                project,
                evaluatorKey,
                () -> Score.builder()
                        .project(project)
                        .evaluatorId(evaluatorKey)
                        .technicalCompleteness(request.getTechnicalCompleteness())
                        .socialValueMajor(request.getSocialValueMajor())
                        .aiUtilizationMajor(request.getAiUtilityMajorScore())
                        .presentationMajor(request.getPresentationMajor())
                        .reportWriting(0).reportContent(0).aiUsagePlan(0)
                        .creativity(0).userExperience(0).socialValueCommunity(0)
                        .aiUtilizationCommunity(0).presentationCommunity(0)
                        .build(),
                score -> score.updateMajorScore(
                        request.getTechnicalCompleteness(), request.getSocialValueMajor(),
                        request.getAiUtilityMajorScore(), request.getPresentationMajor()
                )
        );
    }

    @Transactional
    public void updateMajor(Long projectId, PatchMajorScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, projectId,
                request.getTechnicalCompleteness(), request.getSocialValueMajor(),
                request.getAiUtilityMajorScore(), request.getPresentationMajor());

        Project project = support.getProjectOrThrow(projectId);
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateMajorScore(
                request.getTechnicalCompleteness(), request.getSocialValueMajor(),
                request.getAiUtilityMajorScore(), request.getPresentationMajor()
        );
    }

    @Transactional(readOnly = true)
    public Score getMajor(Long projectId, AuthenticatedUser evaluator) {
        support.requireEvaluatorId(evaluator);
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);

        return support.getScoreOrNull(project, evaluator.userId().toString().trim());
    }

    private void validateEvaluatorAndFields(AuthenticatedUser evaluator, Long projectId,
                                            Integer tech, Integer social, Integer ai, Integer presentation) {
        support.validateEvaluator(evaluator,
                e -> e.adminRole() == AdminRole.MAJOR_TEACHER || e.adminRole() == AdminRole.MASTER,
                "전공 교과 선생님 또는 마스터 권한만 접근 가능합니다.");
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("technicalCompleteness", tech);
        scores.put("socialValueMajor", social);
        scores.put("aiUtilityMajorScore", ai);
        scores.put("presentationMajor", presentation);
        support.requireScores(scores);
    }
}
