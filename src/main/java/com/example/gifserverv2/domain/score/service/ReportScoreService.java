package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
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
public class ReportScoreService {

    private final ScoreSupport support;

    public void createReport(CreateReportScoreRequest request, AuthenticatedUser evaluator) {
        // permission: only grade leader can assign report scores
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.GRADE_HEAD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보고서 영역 점수는 학년부 부장만 부여할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getReportWriting(), "reportWriting");
        support.requireScore(request.getReportContent(), "reportContent");
        support.requireScore(request.getAiUsagePlan(), "aiUsagePlan");
        support.requireScore(request.getCreativity(), "creativity");

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
                        .reportWriting(request.getReportWriting())
                        .reportContent(request.getReportContent())
                        .aiUsagePlan(request.getAiUsagePlan())
                        .creativity(request.getCreativity())
                        .userExperience(0)
                        .socialValueCommunity(0)
                        .aiUtilizationCommunity(0)
                        .presentationCommunity(0)
                        .build(),
                score -> score.updateScore(
                        score.getTechnicalCompleteness(),
                        score.getSocialValueMajor(),
                        score.getAiUtilizationMajor(),
                        score.getPresentationMajor(),
                        request.getReportWriting(),
                        request.getReportContent(),
                        request.getAiUsagePlan(),
                        request.getCreativity(),
                        score.getUserExperience(),
                        score.getSocialValueCommunity(),
                        score.getAiUtilizationCommunity(),
                        score.getPresentationCommunity()
                )
        );
    }

    public void updateReport(CreateReportScoreRequest request, AuthenticatedUser evaluator) {
        // permission: only grade leader can update report scores
        if (evaluator == null || evaluator.adminRole() == null || evaluator.adminRole() != AdminRole.GRADE_HEAD) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보고서 영역 점수는 학년부 부장만 수정할 수 있습니다.");
        }

        support.validateCommonRequest(request.getProjectId(), evaluator.userId().toString());
        support.requireScore(request.getReportWriting(), "reportWriting");
        support.requireScore(request.getReportContent(), "reportContent");
        support.requireScore(request.getAiUsagePlan(), "aiUsagePlan");
        support.requireScore(request.getCreativity(), "creativity");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateScore(
                score.getTechnicalCompleteness(),
                score.getSocialValueMajor(),
                score.getAiUtilizationMajor(),
                score.getPresentationMajor(),
                request.getReportWriting(),
                request.getReportContent(),
                request.getAiUsagePlan(),
                request.getCreativity(),
                score.getUserExperience(),
                score.getSocialValueCommunity(),
                score.getAiUtilizationCommunity(),
                score.getPresentationCommunity()
        );
    }

    public Score getReport(Long projectId, AuthenticatedUser evaluator) {
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluator.userId().toString().trim());
    }
}

