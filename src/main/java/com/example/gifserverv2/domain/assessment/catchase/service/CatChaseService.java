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
import com.example.gifserverv2.domain.assessment.catchase.exception.CatChaseException;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseJudgmentRepository;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseQuestionRepository;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseRoundRepository;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 13;

    private final CatChaseRoundRepository roundRepository;
    private final CatChaseQuestionRepository questionRepository;
    private final CatChaseJudgmentRepository judgmentRepository;
    private final CoinAccountRepository coinAccountRepository;

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

        List<CatChaseJudgment> judgments = items.stream()
                .map(item -> CatChaseJudgment.builder()
                        .roundId(roundId)
                        .questionId(questionId)
                        .position(item.position())
                        .judgment(item.judgment())
                        .confidence(item.confidence())
                        .hadMouse(question.hasMouseAt(item.position()))
                        .build())
                .toList();
        judgmentRepository.saveAll(judgments);

        LocalDateTime now = LocalDateTime.now();
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

    private void completeRound(CatChaseRound round, LocalDateTime now) {
        round.complete(now);
        if (round.isCoinRewarded()) {
            return;
        }
        CoinAccount account = coinAccountRepository.findByUserId(round.getUserId())
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(round.getUserId())));
        account.addCoin(round.calculateCoin());
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
        return questionRepository.save(CatChaseQuestion.issue(
                round.getId(),
                round.getAnsweredCount() + 1,
                randomPositions(),
                randomPositions()
        ));
    }

    private List<Integer> randomPositions() {
        int count = ThreadLocalRandom.current().nextInt(MIN_COUNT, MAX_COUNT + 1);

        List<Integer> allPositions = new ArrayList<>(CatChaseRound.GRID_SIZE);
        for (int i = 0; i < CatChaseRound.GRID_SIZE; i++) {
            allPositions.add(i);
        }
        Collections.shuffle(allPositions);

        return allPositions.subList(0, count).stream().sorted().toList();
    }
}
