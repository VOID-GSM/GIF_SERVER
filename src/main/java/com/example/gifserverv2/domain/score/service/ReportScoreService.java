package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchReportScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportScoreService {

    private final ScoreSupport support;

    public void createReport(CreateReportScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, request.getProjectId(),
                request.getReportWriting(), request.getReportContent(),
                request.getAiUsagePlan(), request.getCreativity());

        Project project = support.getProjectOrThrow(request.getProjectId());
        final String evaluatorKey = evaluator.userId().toString().trim();

        support.upsertScore(
                project,
                evaluatorKey,
                () -> Score.builder()
                        .project(project)
                        .evaluatorId(evaluatorKey)
                        .technicalCompleteness(0).socialValueMajor(0).aiUtilizationMajor(0).presentationMajor(0)
                        .reportWriting(request.getReportWriting())
                        .reportContent(request.getReportContent())
                        .aiUsagePlan(request.getAiUsagePlan())
                        .creativity(request.getCreativity())
                        .userExperience(0).socialValueCommunity(0).aiUtilizationCommunity(0).presentationCommunity(0)
                        .build(),
                score -> score.updateReportScore(
                        request.getReportWriting(), request.getReportContent(),
                        request.getAiUsagePlan(), request.getCreativity()
                )
        );
    }

    public void updateReport(Long projectId, PatchReportScoreRequest request, AuthenticatedUser evaluator) {
        validateEvaluatorAndFields(evaluator, projectId,
                request.getReportWriting(), request.getReportContent(),
                request.getAiUsagePlan(), request.getCreativity());

        Project project = support.getProjectOrThrow(projectId);
        Score score = support.getScoreOrThrow(project, evaluator.userId().toString().trim());

        score.updateReportScore(
                request.getReportWriting(), request.getReportContent(),
                request.getAiUsagePlan(), request.getCreativity()
        );
    }

    @Transactional(readOnly = true)
    public Score getReport(Long projectId, AuthenticatedUser evaluator) {
        if (evaluator == null || evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluator.userId().toString().trim());
    }

    private void validateEvaluatorAndFields(AuthenticatedUser evaluator, Long projectId,
                                            Integer writing, Integer content, Integer plan, Integer creativity) {
        if (evaluator == null || !evaluator.gradeHead()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "보고서 영역 점수는 학년부 부장만 접근 가능합니다.");
        }
        if (evaluator.userId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "평가자 ID가 필요합니다.");
        }

        support.validateCommonRequest(projectId, evaluator.userId().toString());
        support.requireScore(writing, "reportWriting");
        support.requireScore(content, "reportContent");
        support.requireScore(plan, "aiUsagePlan");
        support.requireScore(creativity, "creativity");
    }
}