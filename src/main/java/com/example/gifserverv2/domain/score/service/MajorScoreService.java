package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateMajorScoreRequest;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class MajorScoreService {

    private final ScoreSupport support;

    public void createMajor(CreateMajorScoreRequest request) {
        support.validateCommonRequest(request.getProjectId(), request.getEvaluatorId());
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

        Project project = support.getProjectOrThrow(request.getProjectId());
        if (support.scoreRepository().existsByProjectAndEvaluatorId(project, request.getEvaluatorId().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 평가자의 점수가 존재합니다.");
        }

        Score score = Score.builder()
                .project(project)
                .evaluatorId(request.getEvaluatorId().trim())
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
                .build();

        support.scoreRepository().save(score);
    }

    public void updateMajor(CreateMajorScoreRequest request) {
        support.validateCommonRequest(request.getProjectId(), request.getEvaluatorId());
        support.requireScore(request.getTechnicalCompleteness(), "technicalCompleteness");
        support.requireScore(request.getSocialValueMajor(), "socialValueMajor");
        support.requireScore(request.getAiUtilityMajorScore(), "aiUtilityMajorScore");
        support.requireScore(request.getPresentationMajor(), "presentationMajor");

        Project project = support.getProjectOrThrow(request.getProjectId());
        Score score = support.getScoreOrThrow(project, request.getEvaluatorId().trim());

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

    public Score getMajor(Long projectId, String evaluatorId) {
        support.validateCommonRequest(projectId, evaluatorId);
        Project project = support.getProjectOrThrow(projectId);
        return support.getScoreOrThrow(project, evaluatorId.trim());
    }
}

