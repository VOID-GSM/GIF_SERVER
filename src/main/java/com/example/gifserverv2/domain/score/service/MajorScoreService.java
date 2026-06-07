package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateMajorScoreRequest;
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
        // permission: only MAJOR_TEACHER can assign major scores
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.MAJOR_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "전공 교과 선생님만 전공 점수를 부여할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

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
                        .reportWriting(0)
                        .reportContent(0)
                        .aiUsagePlan(0)
                        .creativity(0)
                        .userExperience(0)
                        .socialValueCommunity(0)
                        .aiUtilizationCommunity(0)
                        .presentationCommunity(0)
                        .build(),
                score -> score.updateScore(
                        request.getTechnicalCompleteness(),
                        request.getSocialValueMajor(),
                        request.getAiUtilityMajorScore(),
                        request.getPresentationMajor(),
                        score.getReportWriting(),
                        score.getReportContent(),
                        score.getAiUsagePlan(),
                        score.getCreativity(),
                        score.getUserExperience(),
                        score.getSocialValueCommunity(),
                        score.getAiUtilizationCommunity(),
                        score.getPresentationCommunity()
                )
        );
    }

    public void updateMajor(CreateMajorScoreRequest request, AuthenticatedUser evaluator) {
        // permission: only MAJOR_TEACHER can update major scores
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.MAJOR_TEACHER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "전공 교과 선생님만 전공 점수를 수정할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateScore(
                request.getTechnicalCompleteness(),
                request.getSocialValueMajor(),
                request.getAiUtilityMajorScore(),
                request.getPresentationMajor(),
                score.getReportWriting(),
                score.getReportContent(),
                score.getAiUsagePlan(),
                score.getCreativity(),
                score.getUserExperience(),
                score.getSocialValueCommunity(),
                score.getAiUtilizationCommunity(),
                score.getPresentationCommunity()
        );
    }

    public Score getMajor(Long projectId, AuthenticatedUser evaluator) {
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluator.userId().toString().trim());
    }
}

