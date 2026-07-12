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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class MajorScoreService {

    private final ScoreSupport support;

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
                                            Integer tech, Integer social, Integer ai, Integer presentation) {
        if (evaluator == null || evaluator.adminRole() == null ||
                (evaluator.adminRole() != AdminRole.MAJOR_TEACHER && evaluator.adminRole() != AdminRole.MASTER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "전공 교과 선생님 또는 마스터 권한만 접근 가능합니다.");
        }
        if (evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }

        support.validateCommonRequest(projectId, evaluator.userId().toString());
        support.requireScore(tech, "technicalCompleteness");
        support.requireScore(social, "socialValueMajor");
        support.requireScore(ai, "aiUtilityMajorScore");
        support.requireScore(presentation, "presentationMajor");
    }
}
