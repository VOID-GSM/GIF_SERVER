package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.dto.response.ScoreNoticeResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreSummaryResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreRankResponse;
import com.example.gifserverv2.domain.score.entity.ScoreNotice;
import com.example.gifserverv2.domain.score.entity.Score;
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
        List<Score> allScores = scoreRepository.findAll();
        Map<Long, List<Score>> scoresByProject =
                allScores.stream().collect(Collectors.groupingBy(s -> s.getProject().getId()));

        List<ScoreSummaryResponse> summaries = new ArrayList<>();

        for (Project p : projects) {
            List<Score> scores = scoresByProject.getOrDefault(p.getId(), List.of());
            int count = scores.size();
            double avg = 0.0;
            if (count > 0) {
                double sum = scores.stream().mapToInt(Score::getSubTotalScore).sum();
                avg = sum / count;
            }
            summaries.add(new ScoreSummaryResponse(p.getId(), p.getTeamName(), avg, count));
        }

        try {
            String snapshot = objectMapper.writeValueAsString(summaries);
            ScoreNotice notice = new ScoreNotice(true, Instant.now(), snapshot, caller.userId());
            scoreNoticeRepository.save(notice);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 저장 중 오류가 발생했습니다.", e);
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
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 데이터 파싱 중 오류가 발생했습니다.", e);
                    }
                })
                .orElseGet(() -> new ScoreNoticeResponse(false, null, List.of()));
    }

    @Transactional(readOnly = true)
    public List<ScoreRankResponse> getRankByGrade(Integer grade) {
        List<Project> projects;
        if (grade == null) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findByGrade(grade);
        }

        List<Score> allScores = scoreRepository.findAll();
        Map<Long, List<Score>> scoresByProject =
                allScores.stream().collect(Collectors.groupingBy(s -> s.getProject().getId()));

        List<ScoreRankResponse> results = new ArrayList<>();

        for (Project p : projects) {
            List<Score> scores = scoresByProject.getOrDefault(p.getId(), List.of());
            int count = scores.size();
            double avg = 0.0;
            if (count > 0) {
                double sum = scores.stream().mapToInt(Score::getSubTotalScore).sum();
                avg = sum / count;
            }
            int totalScore = (int) Math.round(avg);
            results.add(new ScoreRankResponse(0, p.getTeamName(), totalScore));
        }

        results.sort((a, b) -> Integer.compare(b.totalScore(), a.totalScore()));

        int prevScore = Integer.MIN_VALUE;
        int prevRank = 0;
        for (int i = 0; i < results.size(); i++) {
            ScoreRankResponse r = results.get(i);
            int currentScore = r.totalScore();
            int currentRank;
            if (i == 0) {
                currentRank = 1;
            } else if (currentScore == prevScore) {
                currentRank = prevRank;
            } else {
                currentRank = i + 1; // standard competition ranking: ranks skip after ties
            }
            results.set(i, new ScoreRankResponse(currentRank, r.teamName(), currentScore));
            prevScore = currentScore;
            prevRank = currentRank;
        }

        return results;
    }
}
