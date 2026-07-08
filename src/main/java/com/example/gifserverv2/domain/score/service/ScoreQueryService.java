package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.dto.response.GetProjectFieldAverageResponse;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreQueryService {

    private final ScoreRepository scoreRepository;
    private final ProjectRepository projectRepository;

    public GetProjectFieldAverageResponse getProjectFieldAverages(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        List<Score> scores = scoreRepository.findByProject(project);

        if (scores.isEmpty()) {
            return new GetProjectFieldAverageResponse(projectId, 0, 0, 0, 0);
        }

        double majorAvg = scores.stream()
                .mapToDouble(score -> score.getTechnicalCompleteness() + score.getSocialValueMajor()
                        + score.getAiUtilizationMajor() + score.getPresentationMajor())
                .average().orElse(0.0);

        double reportAvg = scores.stream()
                .mapToDouble(score -> score.getReportWriting() + score.getReportContent() + score.getAiUsagePlan())
                .average().orElse(0.0);

        double communityAvg = scores.stream()
                .mapToDouble(score -> score.getCreativity() + score.getUserExperience()
                        + score.getSocialValueCommunity() + score.getAiUtilizationCommunity() + score.getPresentationCommunity())
                .average().orElse(0.0);

        double grandTotalAvg = majorAvg + reportAvg + communityAvg;

        return new GetProjectFieldAverageResponse(
                projectId,
                (int) Math.round(majorAvg),
                (int) Math.round(reportAvg),
                (int) Math.round(communityAvg),
                (int) Math.round(grandTotalAvg)
        );
    }

    public List<GetProjectFieldAverageResponse> getAllProjectFieldAverages() {
        List<Project> projects = projectRepository.findAll();
        List<GetProjectFieldAverageResponse> responses = new ArrayList<>();

        for (Project p : projects) {
            responses.add(getProjectFieldAverages(p.getId()));
        }

        return responses;
    }
}