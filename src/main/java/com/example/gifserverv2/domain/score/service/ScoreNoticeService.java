package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.dto.response.ScoreNoticeResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreSummaryResponse;
import com.example.gifserverv2.domain.score.entity.ScoreNotice;
import com.example.gifserverv2.domain.score.repository.ScoreNoticeRepository;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.user.entity.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ScoreNoticeService {

    private final ProjectRepository projectRepository;
    private final ScoreRepository scoreRepository;
    private final ScoreNoticeRepository scoreNoticeRepository;
    private final ObjectMapper objectMapper;

    public ScoreNoticeService(ProjectRepository projectRepository, ScoreRepository scoreRepository, ScoreNoticeRepository scoreNoticeRepository, ObjectMapper objectMapper) {
        this.projectRepository = projectRepository;
        this.scoreRepository = scoreRepository;
        this.scoreNoticeRepository = scoreNoticeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publish(AuthenticatedUser caller) {
        if (caller == null || caller.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자(선생님)만 공지할 수 있습니다.");
        }

        List<Project> projects = projectRepository.findAll();
        List<com.example.gifserverv2.domain.score.entity.Score> allScores = scoreRepository.findAll();
        // group scores by project id to avoid N+1 queries
        java.util.Map<Long, java.util.List<com.example.gifserverv2.domain.score.entity.Score>> scoresByProject =
                allScores.stream().collect(java.util.stream.Collectors.groupingBy(s -> s.getProject().getId()));

        List<ScoreSummaryResponse> summaries = new ArrayList<>();

        for (Project p : projects) {
            java.util.List<com.example.gifserverv2.domain.score.entity.Score> scores = scoresByProject.getOrDefault(p.getId(), java.util.List.of());
            int count = scores.size();
            double avg = 0.0;
            if (count > 0) {
                double sum = scores.stream().mapToInt(com.example.gifserverv2.domain.score.entity.Score::getSubTotalScore).sum();
                avg = sum / count;
            }
            summaries.add(new ScoreSummaryResponse(p.getId(), p.getTeamName(), avg, count));
        }

        try {
            String snapshot = objectMapper.writeValueAsString(summaries);
            ScoreNotice notice = new ScoreNotice(true, Instant.now(), snapshot, caller.userId());
            scoreNoticeRepository.save(notice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 저장 중 오류가 발생했습니다.");
        }
    }

    @Transactional(readOnly = true)
    public ScoreNoticeResponse getCurrentNotice() {
        return scoreNoticeRepository.findTopByOrderByPublishedAtDesc()
                .map(n -> {
                    try {
                        List<ScoreSummaryResponse> summaries = objectMapper.readValue(n.getSnapshot(), new TypeReference<List<ScoreSummaryResponse>>(){});
                        return new ScoreNoticeResponse(true, n.getPublishedAt(), summaries);
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 데이터 파싱 중 오류가 발생했습니다.");
                    }
                })
                .orElseGet(() -> new ScoreNoticeResponse(false, null, List.of()));
    }
}
