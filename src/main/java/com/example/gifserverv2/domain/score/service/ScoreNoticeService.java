package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.score.dto.response.GetScoreNoticeResponse;
import com.example.gifserverv2.domain.score.dto.response.GetScoreSummaryResponse;
import com.example.gifserverv2.domain.score.dto.response.GetScoreRankResponse;
import com.example.gifserverv2.domain.score.entity.ScoreNotice;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.repository.ScoreNoticeRepository;
import com.example.gifserverv2.domain.score.repository.ScoreRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ScoreNoticeService {

    private final ProjectRepository projectRepository;
    private final ScoreRepository scoreRepository;
    private final ScoreNoticeRepository scoreNoticeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PushSenderService pushSenderService;

    @Transactional
    public void publish(AuthenticatedUser caller) {
        if (caller.adminRole() != AdminRole.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "성적 공지 권한이 없습니다. (Master 선생님 전용)");
        }

        List<Project> projects = projectRepository.findAll();
        List<Score> allScores = scoreRepository.findAll();
        Map<Long, List<Score>> scoresByProject =
                allScores.stream().collect(Collectors.groupingBy(s -> s.getProject().getId()));

        List<GetScoreSummaryResponse> summaries = new ArrayList<>();

        for (Project p : projects) {
            List<Score> scores = scoresByProject.getOrDefault(p.getId(), List.of());
            int count = scores.size();
            double avg = 0.0;
            if (count > 0) {
                double sum = scores.stream().mapToInt(Score::getSubTotalScore).sum();
                avg = sum / count;
            }
            summaries.add(new GetScoreSummaryResponse(p.getId(), p.getTeamName(), avg, count));
        }

        try {
            String snapshot = objectMapper.writeValueAsString(summaries);
            ScoreNotice notice = new ScoreNotice(true, Instant.now(), snapshot, caller.userId());
            scoreNoticeRepository.save(notice);

            List<UserEntity> allUsers = userRepository.findAll();

            for (UserEntity user : allUsers) {
                pushSenderService.sendNotification(
                        user.getId(),
                        PushMessageTemplate.SCORE_PUBLISHED.getTitle(),
                        PushMessageTemplate.SCORE_PUBLISHED.getBody()
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public GetScoreNoticeResponse getCurrentNotice() {
        return scoreNoticeRepository.findTopByOrderByPublishedAtDesc()
                .map(n -> {
                    try {
                        List<GetScoreSummaryResponse> summaries = objectMapper.readValue(n.getSnapshot(), new TypeReference<List<GetScoreSummaryResponse>>(){});
                        return new GetScoreNoticeResponse(true, n.getPublishedAt(), summaries);
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "공지 데이터 파싱 중 오류가 발생했습니다.", e);
                    }
                })
                .orElseGet(() -> new GetScoreNoticeResponse(false, null, List.of()));
    }

    @Transactional(readOnly = true)
    public List<GetScoreRankResponse> getRankByGradeAndRank(Integer grade, Integer targetRank) {
        List<Project> projects = (grade == null) ? projectRepository.findAll() : projectRepository.findByGrade(grade);

        if (projects.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .collect(Collectors.toList());

        List<Score> targetScores = scoreRepository.findByProjectIds(projectIds);

        Map<Long, List<Score>> scoresByProject = targetScores.stream()
                .collect(Collectors.groupingBy(s -> s.getProject().getId()));

        List<GetScoreRankResponse> results = new ArrayList<>();
        for (Project p : projects) {
            List<Score> scores = scoresByProject.getOrDefault(p.getId(), List.of());
            double avg = scores.isEmpty() ? 0.0 : scores.stream().mapToInt(Score::getSubTotalScore).sum() / (double) scores.size();
            results.add(new GetScoreRankResponse(0, p.getTeamName(), (int) Math.round(avg)));
        }

        results.sort((a, b) -> Integer.compare(b.totalScore(), a.totalScore()));

        List<GetScoreRankResponse> rankedResults = new ArrayList<>();
        int prevScore = Integer.MIN_VALUE;
        int prevRank = 0;
        for (int i = 0; i < results.size(); i++) {
            GetScoreRankResponse r = results.get(i);
            int currentScore = r.totalScore();
            int currentRank = (i == 0) ? 1 : (currentScore == prevScore) ? prevRank : i + 1;

            rankedResults.add(new GetScoreRankResponse(currentRank, r.teamName(), currentScore));
            prevScore = currentScore;
            prevRank = currentRank;
        }

        if (targetRank != null) {
            return rankedResults.stream()
                    .filter(r -> r.rank() == targetRank)
                    .collect(Collectors.toList());
        }

        return rankedResults;
    }
}
