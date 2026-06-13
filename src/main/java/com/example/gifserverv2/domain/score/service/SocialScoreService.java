package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateSocialScoreRequest;
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
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.GENERAL_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사회 중심 영역 점수는 일반 교과 선생님만 부여할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getUserExperience(), "userExperience");
        support.requireScore(request.getSocialValueCommunity(), "socialValueCommunity");
        support.requireScore(request.getAiUtilizationCommunity(), "aiUtilizationCommunity");
        support.requireScore(request.getPresentationCommunity(), "presentationCommunity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluator.userId().toString().trim();
        support.upsertScore(
                project,
                evaluatorKey,
                () -> Score.builder()
                        .project(project)
                        .evaluatorId(evaluatorKey)
                        .technicalCompleteness(0)
                        .socialValueMajor(0)
                        .aiUtilizationMajor(0)
                        .presentationMajor(0)
                        .reportWriting(0)
                        .reportContent(0)
                        .aiUsagePlan(0)
                        .creativity(0)
                        .userExperience(request.getUserExperience())
                        .socialValueCommunity(request.getSocialValueCommunity())
                        .aiUtilizationCommunity(request.getAiUtilizationCommunity())
                        .presentationCommunity(request.getPresentationCommunity())
                        .build(),
                score -> score.updateScore(
                        score.getTechnicalCompleteness(),
                        score.getSocialValueMajor(),
                        score.getAiUtilizationMajor(),
                        score.getPresentationMajor(),
                        score.getReportWriting(),
                        score.getReportContent(),
                        score.getAiUsagePlan(),
                        score.getCreativity(),
                        request.getUserExperience(),
                        request.getSocialValueCommunity(),
                        request.getAiUtilizationCommunity(),
                        request.getPresentationCommunity()
                )
        );
    }

    public void updateSocial(CreateSocialScoreRequest request, AuthenticatedUser evaluator) {
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.GENERAL_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "사회 중심 영역 점수는 일반 교과 선생님만 수정할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getUserExperience(), "userExperience");
        support.requireScore(request.getSocialValueCommunity(), "socialValueCommunity");
        support.requireScore(request.getAiUtilizationCommunity(), "aiUtilizationCommunity");
        support.requireScore(request.getPresentationCommunity(), "presentationCommunity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateScore(
                score.getTechnicalCompleteness(),
                score.getSocialValueMajor(),
                score.getAiUtilizationMajor(),
                score.getPresentationMajor(),
                score.getReportWriting(),
                score.getReportContent(),
                score.getAiUsagePlan(),
                score.getCreativity(),
                request.getUserExperience(),
                request.getSocialValueCommunity(),
                request.getAiUtilizationCommunity(),
                request.getPresentationCommunity()
        );
    }

    public Score getSocial(Long projectId, AuthenticatedUser evaluator) {
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluator.userId().toString().trim());
    }
}

