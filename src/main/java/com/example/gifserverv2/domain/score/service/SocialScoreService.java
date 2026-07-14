package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateSocialScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchSocialScoreRequest;
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
public class SocialScoreService {

    private final ScoreSupport support;

    @Transactional
    public void createSocial(CreateSocialScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, request.getProjectId(),
                request.getUserExperience(), request.getSocialValueCommunity(),
                request.getAiUtilizationCommunity(), request.getPresentationCommunity());

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluator.userId().toString().trim();

        support.upsertScore(
                project,
                evaluatorKey,
                () -> Score.builder()
                        .project(project)
                        .evaluatorId(evaluatorKey)
                        .technicalCompleteness(0).socialValueMajor(0).aiUtilizationMajor(0).presentationMajor(0)
                        .reportWriting(0).reportContent(0).aiUsagePlan(0).creativity(0)
                        .userExperience(request.getUserExperience())
                        .socialValueCommunity(request.getSocialValueCommunity())
                        .aiUtilizationCommunity(request.getAiUtilizationCommunity())
                        .presentationCommunity(request.getPresentationCommunity())
                        .build(),
                score -> score.updateSocialScore(
                        request.getUserExperience(), request.getSocialValueCommunity(),
                        request.getAiUtilizationCommunity(), request.getPresentationCommunity()
                )
        );
    }

    @Transactional
    public void updateSocial(Long projectId, PatchSocialScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, projectId,
                request.getUserExperience(), request.getSocialValueCommunity(),
                request.getAiUtilizationCommunity(), request.getPresentationCommunity());

        Project project = support.getProjectOrThrow(projectId);
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateSocialScore(
                request.getUserExperience(), request.getSocialValueCommunity(),
                request.getAiUtilizationCommunity(), request.getPresentationCommunity()
        );
    }

    @Transactional(readOnly = true)
    public Score getSocial(Long projectId, AuthenticatedUser evaluator) {
        support.requireEvaluatorId(evaluator);
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);

        return support.getScoreOrNull(project, evaluator.userId().toString().trim());
    }

    private void validateEvaluatorAndFields(AuthenticatedUser evaluator, Long projectId,
                                            Integer userExp, Integer socialVal, Integer aiUtil, Integer presentation) {
        support.validateEvaluator(evaluator, e -> e.adminRole() == AdminRole.GENERAL_TEACHER,
                "사회 중심 영역 점수는 일반 교과 선생님만 접근 가능합니다.");
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("userExperience", userExp);
        scores.put("socialValueCommunity", socialVal);
        scores.put("aiUtilizationCommunity", aiUtil);
        scores.put("presentationCommunity", presentation);
        support.requireScores(scores);
    }
}