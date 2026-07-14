package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchReportScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.gifserverv2.global.security.AuthenticatedUser;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportScoreService {

    private final ScoreSupport support;

    @Transactional
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

    @Transactional
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
        support.requireEvaluatorId(evaluator);
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Project project = support.getProjectOrThrow(projectId);

        return support.getScoreOrNull(project, evaluator.userId().toString().trim());
    }

    private void validateEvaluatorAndFields(AuthenticatedUser evaluator, Long projectId,
                                            Integer writing, Integer content, Integer plan, Integer creativity) {
        support.validateEvaluator(evaluator, AuthenticatedUser::gradeHead,
                "보고서 영역 점수는 학년부 부장만 접근 가능합니다.");
        support.validateCommonRequest(projectId, evaluator.userId().toString());
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("reportWriting", writing);
        scores.put("reportContent", content);
        scores.put("aiUsagePlan", plan);
        scores.put("creativity", creativity);
        support.requireScores(scores);
    }
}