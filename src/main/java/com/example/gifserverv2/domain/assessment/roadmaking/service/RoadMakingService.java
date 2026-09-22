package com.example.gifserverv2.domain.assessment.roadmaking.service;

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

    @Transactional
    public StartRoadMakingRoundResponse startRound(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        Optional<RoadMakingRound> active =
                roundRepository.findFirstByUserIdAndStatus(userId, RoundStatus.IN_PROGRESS);
        if (active.isPresent()) {
            RoadMakingRound round = active.get();
            if (!round.isExpired(now)) {
                RoadMakingProblem current = findCurrentProblem(round);
                return StartRoadMakingRoundResponse.of(round, toProblemResponse(current), now);
            }
            closeRound(round, now);
        }

        RoadMakingRound round = roundRepository.save(RoadMakingRound.start(userId, now, TIME_LIMIT));
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
            closeRound(round, now);
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
            closeRound(round, now);
            return SubmitRoadMakingAnswerResponse.timeOver(round, RoadMakingRoundResultResponse.from(round));
        }

        RoadMakingProblem problem = problemRepository.findByIdAndRoundId(problemId, roundId)
                .orElseThrow(() -> new RoadMakingException(RoadMakingErrorCode.PROBLEM_NOT_FOUND));
        if (problem.isSolved()) {
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

        problem.increaseAttempt();
        if (!correct) {
            round.markWrong();
            return SubmitRoadMakingAnswerResponse.of(false, round, results, null);
        }

        problem.markSolved(now);
        round.markSolved();
        RoadMakingProblem next = issueProblem(round, now);
        return SubmitRoadMakingAnswerResponse.of(true, round, results, toProblemResponse(next));
    }

    @Transactional
    public RoadMakingRoundResultResponse finishRound(Long userId, Long roundId) {
        RoadMakingRound round = getOwnedRound(userId, roundId);
        if (round.isInProgress()) {
            closeRound(round, LocalDateTime.now());
        }
        return RoadMakingRoundResultResponse.from(round);
    }

    private void closeRound(RoadMakingRound round, LocalDateTime now) {
        round.finish(now);
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
        return problemRepository.findFirstByRoundIdAndSolvedFalseOrderBySequenceAsc(round.getId())
                .orElseThrow(() -> new RoadMakingException(RoadMakingErrorCode.PROBLEM_NOT_FOUND));
    }

    private RoadMakingProblem issueProblem(RoadMakingRound round, LocalDateTime now) {
        GeneratedProblem generated = problemGenerator.generate(round.getSolvedCount());
        return problemRepository.save(RoadMakingProblem.issue(
                round,
                round.getSolvedCount() + 1,
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