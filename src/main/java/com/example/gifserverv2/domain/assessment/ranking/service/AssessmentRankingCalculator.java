package com.example.gifserverv2.domain.assessment.ranking.service;

import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse.RankingEntry;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 사용자별 최고 기록으로 순위를 매긴다.
 * 정렬: 맞힌 수 ↓ → 정답률 ↓ → 소요 시간 ↑ → 먼저 달성한 순
 */
@Component
@RequiredArgsConstructor
public class AssessmentRankingCalculator {

    public static final int DEFAULT_LIMIT = 7;
    private static final int MAX_LIMIT = 100;

    static final Comparator<AssessmentRecord> RANKING_ORDER =
            Comparator.comparingInt(AssessmentRecord::correctCount).reversed()
                    .thenComparing(AssessmentRankingCalculator::compareAccuracyDesc)
                    .thenComparingLong(AssessmentRecord::elapsedSeconds)
                    .thenComparing(AssessmentRecord::achievedAt)
                    .thenComparing(AssessmentRecord::userId);

    private final UserRepository userRepository;

    public AssessmentRankingResponse rank(List<AssessmentRecord> records, Long myUserId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

        List<AssessmentRecord> bestPerUser = new ArrayList<>(records.stream()
                .collect(Collectors.toMap(AssessmentRecord::userId, Function.identity(),
                        (a, b) -> RANKING_ORDER.compare(a, b) <= 0 ? a : b,
                        LinkedHashMap::new))
                .values());
        bestPerUser.sort(RANKING_ORDER);

        List<AssessmentRecord> top = bestPerUser.subList(0, Math.min(safeLimit, bestPerUser.size()));
        int myIndex = indexOf(bestPerUser, myUserId);

        Set<Long> userIds = new HashSet<>();
        top.forEach(record -> userIds.add(record.userId()));
        if (myIndex >= 0) {
            userIds.add(myUserId);
        }
        Map<Long, UserEntity> users = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        List<RankingEntry> rankings = new ArrayList<>(top.size());
        for (int i = 0; i < top.size(); i++) {
            rankings.add(toEntry(i + 1, top.get(i), users.get(top.get(i).userId())));
        }
        RankingEntry me = myIndex < 0 ? null
                : toEntry(myIndex + 1, bestPerUser.get(myIndex), users.get(myUserId));

        return new AssessmentRankingResponse(rankings, me);
    }

    private static int compareAccuracyDesc(AssessmentRecord a, AssessmentRecord b) {
        // a.correct/a.total 와 b.correct/b.total 을 나눗셈 없이 비교
        long left = (long) b.correctCount() * a.totalCount();
        long right = (long) a.correctCount() * b.totalCount();
        return Long.compare(left, right);
    }

    private static int indexOf(List<AssessmentRecord> records, Long userId) {
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).userId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    private static RankingEntry toEntry(int rank, AssessmentRecord record, UserEntity user) {
        String name = user != null ? user.getName() : "알 수 없음";
        Integer grade = user != null ? parseGrade(user.getGrade()) : null;
        return new RankingEntry(rank, name, grade, record.correctCount(), record.totalCount(), record.earnedCoin());
    }

    private static Integer parseGrade(String grade) {
        if (grade == null || grade.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(grade.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
