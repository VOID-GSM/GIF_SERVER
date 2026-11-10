package com.example.gifserverv2.domain.assessment.catchase.service;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundResultResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundSummaryResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.StartCatChaseRoundResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.SubmitCatChaseJudgmentsResponse;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.entity.Judgment;
import com.example.gifserverv2.domain.assessment.catchase.exception.CatChaseException;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseJudgmentRepository;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseQuestionRepository;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseRoundRepository;
import com.example.gifserverv2.domain.assessment.common.AssessmentCoinRewarder;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.domain.assessment.ranking.service.AssessmentRankingCalculator;
import com.example.gifserverv2.domain.assessment.ranking.service.AssessmentRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatChaseService {

    private static final int MIN_MOUSE_COUNT = 1;
    private static final int MAX_MOUSE_COUNT = 13;
    // catPositions[0] = 빨간 고양이, catPositions[1] = 파란 고양이
    private static final int CAT_COUNT = 2;
    // 고양이 1마리당 10초. 네트워크 지연을 고려해 여유 시간을 더한다
    private static final Duration TIME_LIMIT = Duration.ofSeconds(10L * CAT_COUNT);
    private static final Duration GRACE = Duration.ofSeconds(2);

    private final CatChaseRoundRepository roundRepository;
    private final CatChaseQuestionRepository questionRepository;
    private final CatChaseJudgmentRepository judgmentRepository;
    private final AssessmentCoinRewarder coinRewarder;
    private final AssessmentRankingCalculator rankingCalculator;

    @Transactional
    public StartCatChaseRoundResponse startRound(Long userId, StartAssessmentRoundRequest request) {
        CatChaseRound round = roundRepository.save(CatChaseRound.start(userId, request.questionCount()));
        CatChaseQuestion first = issueQuestion(round);
        return StartCatChaseRoundResponse.of(round, CatChaseQuestionResponse.from(first));
    }

    @Transactional(readOnly = true)
    public CatChaseQuestionResponse getCurrentQuestion(Long userId, Long roundId) {
        CatChaseRound round = readOwnedRound(userId, roundId);
        if (!round.isInProgress()) {
            throw CatChaseException.alreadyFinished();
        }
        return CatChaseQuestionResponse.from(findCurrentQuestion(round));
    }

    @Transactional
    public SubmitCatChaseJudgmentsResponse submitJudgments(Long userId, Long roundId, Long questionId,
                                                           SubmitCatChaseJudgmentsRequest request) {
        CatChaseRound round = getOwnedRound(userId, roundId);
        if (!round.isInProgress()) {
            throw CatChaseException.alreadyFinished();
        }

        CatChaseQuestion question = questionRepository.findByIdAndRoundId(questionId, roundId)
                .orElseThrow(CatChaseException::questionNotFound);
        if (question.isAnswered()) {
            throw CatChaseException.alreadySubmitted();
        }

        List<SubmitCatChaseJudgmentsRequest.JudgmentItem> items = request.judgments();
        List<Integer> catPositions = question.getCatPositions();
        Set<Integer> judgedPositions = new HashSet<>();
        for (SubmitCatChaseJudgmentsRequest.JudgmentItem item : items) {
            if (!catPositions.contains(item.position())) {
                throw CatChaseException.invalidPosition();
            }
            judgedPositions.add(item.position());
        }
        // 고양이 위치마다 정확히 한 번씩 판단해야 한다
        if (items.size() != catPositions.size() || judgedPositions.size() != catPositions.size()) {
            throw CatChaseException.judgmentCountMismatch();
        }

        LocalDateTime now = LocalDateTime.now();
        // 클라이언트가 보낸 판단과 관계없이, 제한 시간을 넘긴 제출은 모두 TIMEOUT 으로 기록한다
        boolean timedOut = question.isTimedOut(now, TIME_LIMIT.plus(GRACE));
        List<CatChaseJudgment> judgments = items.stream()
                .map(item -> {
                    Judgment judgment = timedOut ? Judgment.TIMEOUT : item.judgment();
                    return CatChaseJudgment.builder()
                            .roundId(roundId)
                            .questionId(questionId)
                            .position(item.position())
                            .judgment(judgment)
                            .confidence(judgment == Judgment.TIMEOUT ? null : item.confidence())
                            .hadMouse(question.hasMouseAt(item.position()))
                            .build();
                })
                .toList();
        judgmentRepository.saveAll(judgments);

        boolean correct = judgments.stream().allMatch(CatChaseJudgment::isCorrect);
        question.answer(correct, now);
        round.recordAnswer(correct);

        if (round.isAllAnswered()) {
            completeRound(round, now);
            return SubmitCatChaseJudgmentsResponse.of(correct, round, null, CatChaseRoundResultResponse.from(round));
        }

        CatChaseQuestion next = issueQuestion(round);
        return SubmitCatChaseJudgmentsResponse.of(correct, round, CatChaseQuestionResponse.from(next), null);
    }

    @Transactional
    public CatChaseRoundResultResponse finishRound(Long userId, Long roundId) {
        CatChaseRound round = getOwnedRound(userId, roundId);
        if (round.isInProgress()) {
            // 모든 문항을 풀기 전에 직접 종료하면 이탈로 보고 코인을 지급하지 않는다
            round.abandon(LocalDateTime.now());
        }
        return CatChaseRoundResultResponse.from(round);
    }

    @Transactional(readOnly = true)
    public CatChaseRoundDetailResponse getRound(Long userId, Long roundId) {
        CatChaseRound round = readOwnedRound(userId, roundId);
        List<CatChaseQuestion> questions = questionRepository.findAllByRoundIdOrderBySequenceAsc(roundId);
        Map<Long, List<CatChaseJudgment>> judgmentsByQuestion = judgmentRepository.findAllByRoundId(roundId).stream()
                .filter(judgment -> judgment.getQuestionId() != null)
                .collect(Collectors.groupingBy(CatChaseJudgment::getQuestionId));
        return CatChaseRoundDetailResponse.from(round, questions, judgmentsByQuestion);
    }

    @Transactional(readOnly = true)
    public List<CatChaseRoundSummaryResponse> getMyRounds(Long userId) {
        return roundRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(CatChaseRoundSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentRankingResponse getRanking(Long userId, int questionCount, int limit) {
        AssessmentRankingCalculator.validateQuestionCount(questionCount);
        List<AssessmentRecord> records = roundRepository.findAllByCompletedTrueAndQuestionCount(questionCount).stream()
                .map(round -> new AssessmentRecord(
                        round.getUserId(),
                        round.getCorrectCount(),
                        round.getQuestionCount(),
                        Duration.between(round.getCreatedAt(), round.getFinishedAt()).getSeconds(),
                        round.getFinishedAt(),
                        round.getEarnedCoin()))
                .toList();
        return rankingCalculator.rank(questionCount, records, userId, limit);
    }

    private void completeRound(CatChaseRound round, LocalDateTime now) {
        round.complete(now);
        if (round.isCoinRewarded()) {
            return;
        }
        coinRewarder.reward(round.getUserId(), round.calculateCoin());
        round.markCoinRewarded();
    }

    /** 상태를 바꾸는 요청용: 동시 제출을 막기 위해 비관적 락을 건다 */
    private CatChaseRound getOwnedRound(Long userId, Long roundId) {
        return roundRepository.findByIdAndUserId(roundId, userId)
                .orElseThrow(CatChaseException::notFound);
    }

    /** 조회 전용: 락 없이 읽는다 */
    private CatChaseRound readOwnedRound(Long userId, Long roundId) {
        return roundRepository.findById(roundId)
                .filter(round -> round.isOwnedBy(userId))
                .orElseThrow(CatChaseException::notFound);
    }

    private CatChaseQuestion findCurrentQuestion(CatChaseRound round) {
        // 다음 문항은 이전 문항에 응답한 뒤에만 발급되므로, 가장 최근 문항이 현재 문항이다
        return questionRepository.findFirstByRoundIdOrderBySequenceDesc(round.getId())
                .filter(question -> !question.isAnswered())
                .orElseThrow(CatChaseException::questionNotFound);
    }

    private CatChaseQuestion issueQuestion(CatChaseRound round) {
        List<Integer> mousePositions = randomMousePositions();
        return questionRepository.save(CatChaseQuestion.issue(
                round.getId(),
                round.getAnsweredCount() + 1,
                mousePositions,
                randomCatPositions(mousePositions)
        ));
    }

    private List<Integer> randomMousePositions() {
        int count = ThreadLocalRandom.current().nextInt(MIN_MOUSE_COUNT, MAX_MOUSE_COUNT + 1);

        List<Integer> allPositions = new ArrayList<>(CatChaseRound.GRID_SIZE);
        for (int i = 0; i < CatChaseRound.GRID_SIZE; i++) {
            allPositions.add(i);
        }
        Collections.shuffle(allPositions);

        return allPositions.subList(0, count).stream().sorted().toList();
    }

    /**
     * 고양이마다 50% 확률로 쥐 칸에 놓는다. 무작위 칸에 놓으면 쥐와 겹치는 경우가 적어 MISSED 만 골라도 대부분 맞힌다.
     * 쥐 칸에 놓을 고양이 수를 먼저 정하고 마지막에 순서를 섞어서, 특정 색 고양이만 빈 칸에 놓이는 규칙이 생기지 않게 한다.
     */
    static List<Integer> randomCatPositions(List<Integer> mousePositions) {
        List<Integer> mouseCells = new ArrayList<>(mousePositions);
        List<Integer> emptyCells = new ArrayList<>(CatChaseRound.GRID_SIZE);
        for (int i = 0; i < CatChaseRound.GRID_SIZE; i++) {
            if (!mousePositions.contains(i)) {
                emptyCells.add(i);
            }
        }
        Collections.shuffle(mouseCells);
        Collections.shuffle(emptyCells);

        int onMouseCount = 0;
        for (int i = 0; i < CAT_COUNT; i++) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                onMouseCount++;
            }
        }
        // 쥐 칸이 모자라면 빈 칸에 놓는다. 쥐는 최대 13마리라 빈 칸은 항상 고양이 수보다 많다
        onMouseCount = Math.min(onMouseCount, mouseCells.size());

        List<Integer> catPositions = new ArrayList<>(mouseCells.subList(0, onMouseCount));
        catPositions.addAll(emptyCells.subList(0, CAT_COUNT - onMouseCount));
        // 순서가 곧 고양이 색(빨강, 파랑)이므로 정렬하지 않고 섞는다
        Collections.shuffle(catPositions);
        return catPositions;
    }
}
