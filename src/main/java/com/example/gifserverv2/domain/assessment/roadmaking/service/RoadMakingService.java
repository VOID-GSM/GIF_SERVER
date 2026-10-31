package com.example.gifserverv2.domain.assessment.roadmaking.service;

import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.domain.assessment.ranking.service.AssessmentRankingCalculator;
import com.example.gifserverv2.domain.assessment.ranking.service.AssessmentRecord;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.FencePosition;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingProblemResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingRoundResultResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.StartRoadMakingRoundResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.SubmitRoadMakingAnswerRequest;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.SubmitRoadMakingAnswerResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.SubmitRoadMakingAnswerResponse.VehicleResult;
import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingProblem;
import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoadMakingRound;
import com.example.gifserverv2.domain.assessment.roadmaking.entity.RoundStatus;
import com.example.gifserverv2.domain.assessment.roadmaking.exception.RoadMakingErrorCode;
import com.example.gifserverv2.domain.assessment.roadmaking.exception.RoadMakingException;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Board;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Position;
import com.example.gifserverv2.domain.assessment.roadmaking.model.Trace;
import com.example.gifserverv2.domain.assessment.roadmaking.repository.RoadMakingProblemRepository;
import com.example.gifserverv2.domain.assessment.roadmaking.repository.RoadMakingRoundRepository;
import com.example.gifserverv2.domain.assessment.roadmaking.service.ProblemGenerator.GeneratedProblem;
import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadMakingService {
    private static final Duration TIME_LIMIT = Duration.ofMinutes(5);

    private static final Duration GRACE = Duration.ofSeconds(2);

    private final RoadMakingRoundRepository roundRepository;
    private final RoadMakingProblemRepository problemRepository;
    private final ProblemGenerator problemGenerator;
    private final BoardMapper boardMapper;
    private final CoinAccountRepository coinAccountRepository;
    private final AssessmentRankingCalculator rankingCalculator;

    @Transactional
    public StartRoadMakingRoundResponse startRound(Long userId, StartAssessmentRoundRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Optional<RoadMakingRound> active =
                roundRepository.findFirstByUserIdAndStatus(userId, RoundStatus.IN_PROGRESS);
        if (active.isPresent()) {
            RoadMakingRound round = active.get();
            if (!round.isExpired(now)) {
                RoadMakingProblem current = findCurrentProblem(round);
                return StartRoadMakingRoundResponse.of(round, toProblemResponse(current), now);
            }
            completeRound(round, now);
        }

        RoadMakingRound round = roundRepository.save(
                RoadMakingRound.start(userId, request.questionCount(), now, TIME_LIMIT));
        RoadMakingProblem first = issueProblem(round, now);
        return StartRoadMakingRoundResponse.of(round, toProblemResponse(first), now);
    }

    @Transactional(noRollbackFor = RoadMakingException.class)
    public RoadMakingProblemResponse getCurrentProblem(Long userId, Long roundId) {
        LocalDateTime now = LocalDateTime.now();
        RoadMakingRound round = getOwnedRound(userId, roundId);

        if (!round.isInProgress()) {
            throw new RoadMakingException(RoadMakingErrorCode.ROUND_ALREADY_FINISHED);
        }
        if (round.isExpired(now)) {
            completeRound(round, now);
            throw new RoadMakingException(RoadMakingErrorCode.ROUND_TIME_OVER);
        }
        return toProblemResponse(findCurrentProblem(round));
    }

    @Transactional
    public SubmitRoadMakingAnswerResponse submit(Long userId, Long roundId, Long problemId, SubmitRoadMakingAnswerRequest request) {
        LocalDateTime now = LocalDateTime.now();
        RoadMakingRound round = getOwnedRound(userId, roundId);

        if (!round.isInProgress()) {
            throw new RoadMakingException(RoadMakingErrorCode.ROUND_ALREADY_FINISHED);
        }
        if (round.isExpired(now.minus(GRACE))) {
            completeRound(round, now);
            return SubmitRoadMakingAnswerResponse.timeOver(round, RoadMakingRoundResultResponse.from(round));
        }

        RoadMakingProblem problem = problemRepository.findByIdAndRoundId(problemId, roundId)
                .orElseThrow(() -> new RoadMakingException(RoadMakingErrorCode.PROBLEM_NOT_FOUND));
        if (problem.isAnswered()) {
            throw new RoadMakingException(RoadMakingErrorCode.PROBLEM_ALREADY_SOLVED);
        }

        Board board = boardMapper.fromJson(problem.getBoardJson());
        boolean[][] fence = toFenceGrid(board, problem.getMinFenceCount(), request.fences());

        PathSimulator simulator = new PathSimulator(board);
        List<VehicleResult> results = board.vehicles().stream()
                .map(vehicle -> {
                    Trace trace = simulator.trace(vehicle, fence);
                    return new VehicleResult(vehicle.color(), trace.path(), trace.reached());
                })
                .toList();
        boolean correct = results.stream().allMatch(VehicleResult::reached);

        // 한 문항은 한 번만 응답할 수 있고, 맞히든 틀리든 다음 문항으로 넘어간다
        problem.increaseAttempt();
        if (correct) {
            problem.markSolved(now);
            round.markSolved();
        } else {
            problem.markWrong();
            round.markWrong();
        }

        if (round.isAllAnswered()) {
            completeRound(round, now);
            return SubmitRoadMakingAnswerResponse.of(correct, round, results, null,
                    RoadMakingRoundResultResponse.from(round));
        }

        RoadMakingProblem next = issueProblem(round, now);
        return SubmitRoadMakingAnswerResponse.of(correct, round, results, toProblemResponse(next), null);
    }

    @Transactional
    public RoadMakingRoundResultResponse finishRound(Long userId, Long roundId) {
        LocalDateTime now = LocalDateTime.now();
        RoadMakingRound round = getOwnedRound(userId, roundId);
        if (round.isInProgress()) {
            if (round.isExpired(now)) {
                completeRound(round, now);
            } else {
                // 모든 문항을 풀기 전에 직접 종료하면 이탈로 보고 코인을 지급하지 않는다
                round.abandon(now);
            }
        }
        return RoadMakingRoundResultResponse.from(round);
    }

    @Transactional(readOnly = true)
    public AssessmentRankingResponse getRanking(Long userId, int limit) {
        List<AssessmentRecord> records = roundRepository.findAllByCompletedTrueAndQuestionCountGreaterThan(0).stream()
                .map(round -> new AssessmentRecord(
                        round.getUserId(),
                        round.getSolvedCount(),
                        round.getQuestionCount(),
                        Duration.between(round.getStartedAt(), round.getFinishedAt()).getSeconds(),
                        round.getFinishedAt(),
                        round.getEarnedCoin()))
                .toList();
        return rankingCalculator.rank(records, userId, limit);
    }

    /** 전 문항 응답 또는 제한 시간 종료로 끝까지 진행한 라운드: 코인 지급 */
    private void completeRound(RoadMakingRound round, LocalDateTime now) {
        round.complete(now);
        if (round.isCoinRewarded()) {
            return;
        }
        CoinAccount account = coinAccountRepository.findByUserId(round.getUserId())
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(round.getUserId())));
        account.addCoin(round.calculateCoin());
        round.markCoinRewarded();
    }

    private RoadMakingRound getOwnedRound(Long userId, Long roundId) {
        return roundRepository.findByIdAndUserId(roundId, userId)
                .orElseThrow(() -> new RoadMakingException(RoadMakingErrorCode.ROUND_NOT_FOUND));
    }

    private RoadMakingProblem findCurrentProblem(RoadMakingRound round) {
        // 다음 문항은 이전 문항에 응답한 뒤에만 발급되므로, 가장 최근 문항이 현재 문항이다
        return problemRepository.findFirstByRoundIdOrderBySequenceDesc(round.getId())
                .filter(problem -> !problem.isAnswered())
                .orElseThrow(() -> new RoadMakingException(RoadMakingErrorCode.PROBLEM_NOT_FOUND));
    }

    private RoadMakingProblem issueProblem(RoadMakingRound round, LocalDateTime now) {
        GeneratedProblem generated = problemGenerator.generate(round.getSolvedCount());
        return problemRepository.save(RoadMakingProblem.issue(
                round,
                round.getAnsweredCount() + 1,
                generated.minFenceCount(),
                boardMapper.toJson(generated.board()),
                now
        ));
    }

    private RoadMakingProblemResponse toProblemResponse(RoadMakingProblem problem) {
        return RoadMakingProblemResponse.of(problem, boardMapper.fromJson(problem.getBoardJson()));
    }

    private boolean[][] toFenceGrid(Board board, int requiredCount, List<FencePosition> fences) {
        if (fences.size() != requiredCount) {
            throw new RoadMakingException(RoadMakingErrorCode.INVALID_FENCE_COUNT);
        }
        boolean[][] grid = new boolean[board.rows()][board.cols()];
        for (FencePosition fence : fences) {
            Position position = new Position(fence.row(), fence.col());
            if (!board.inBounds(position)
                    || board.isOccupied(position)
                    || grid[position.row()][position.col()]) {
                throw new RoadMakingException(RoadMakingErrorCode.INVALID_FENCE_POSITION);
            }
            grid[position.row()][position.col()] = true;
        }
        return grid;
    }
}
