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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class SocialScoreService {

    private final ScoreSupport support;

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
        if (evaluator == null || evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);

        try {
            return support.getScoreOrThrow(project, evaluator.userId().toString().trim());
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw e;
        }
    }

    private void validateEvaluatorAndFields(AuthenticatedUser evaluator, Long projectId,
                                            Integer userExp, Integer socialVal, Integer aiUtil, Integer presentation) {
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.GENERAL_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사회 중심 영역 점수는 일반 교과 선생님만 접근 가능합니다.");
        }
        if (evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }

        support.validateCommonRequest(projectId, evaluator.userId().toString());
        support.requireScore(userExp, "userExperience");
        support.requireScore(socialVal, "socialValueCommunity");
        support.requireScore(aiUtil, "aiUtilizationCommunity");
        support.requireScore(presentation, "presentationCommunity");
    }
}