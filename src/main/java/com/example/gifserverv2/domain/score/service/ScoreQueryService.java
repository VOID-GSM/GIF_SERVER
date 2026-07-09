package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.dto.response.GetProjectFieldAverageResponse;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScoreQueryService {

    private final ScoreRepository scoreRepository;
    private final ProjectRepository projectRepository;

    public GetProjectFieldAverageResponse getProjectFieldAverages(Long projectId, AuthenticatedUser user) {
        validateAdminAuthority(user);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다."));

        List<Score> scores = scoreRepository.findByProject(project);

        return calculateProjectAverage(projectId, scores);
    }

    public List<GetProjectFieldAverageResponse> getAllProjectFieldAverages(AuthenticatedUser user) {
        validateAdminAuthority(user);

        List<Project> projects = projectRepository.findAll();
        List<Score> allScores = scoreRepository.findAll();

        Map<Long, List<Score>> scoresByProject = allScores.stream()
                .collect(Collectors.groupingBy(score -> score.getProject().getId()));

        List<GetProjectFieldAverageResponse> responses = new ArrayList<>();
        for (Project p : projects) {
            List<Score> scores = scoresByProject.getOrDefault(p.getId(), List.of());
            responses.add(calculateProjectAverage(p.getId(), scores));
        }

        return responses;
    }

    private GetProjectFieldAverageResponse calculateProjectAverage(Long projectId, List<Score> scores) {
        if (scores.isEmpty()) {
            return new GetProjectFieldAverageResponse(projectId, 0, 0, 0, 0);
        }

        double majorAvg = scores.stream()
                .mapToDouble(score -> safe(score.getTechnicalCompleteness()) + safe(score.getSocialValueMajor())
                        + safe(score.getAiUtilizationMajor()) + safe(score.getPresentationMajor()))
                .average().orElse(0.0);

        double reportAvg = scores.stream()
                .mapToDouble(score -> safe(score.getReportWriting()) + safe(score.getReportContent())
                        + safe(score.getAiUsagePlan()) + safe(score.getCreativity()))
                .average().orElse(0.0);

        double communityAvg = scores.stream()
                .mapToDouble(score -> safe(score.getUserExperience()) + safe(score.getSocialValueCommunity())
                        + safe(score.getAiUtilizationCommunity()) + safe(score.getPresentationCommunity()))
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

    private int safe(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

    private void validateAdminAuthority(AuthenticatedUser user) {
        if (user == null || user.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "조회 권한이 없습니다. 관리자(선생님)만 접근 가능합니다.");
        }
    }
}