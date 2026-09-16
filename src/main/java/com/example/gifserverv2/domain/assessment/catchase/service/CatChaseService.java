package com.example.gifserverv2.domain.assessment.catchase.service;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundSummaryResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.StartCatChaseRoundResponse;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseJudgment;
import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseRound;
import com.example.gifserverv2.domain.assessment.catchase.exception.CatChaseException;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseJudgmentRepository;
import com.example.gifserverv2.domain.assessment.catchase.repository.CatChaseRoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CatChaseService {

    private static final int MIN_COUNT = 1;
    private static final int MAX_COUNT = 13;

    private final CatChaseRoundRepository roundRepository;
    private final CatChaseJudgmentRepository judgmentRepository;

    @Transactional
    public StartCatChaseRoundResponse startRound(Long userId) {
        CatChaseRound round = CatChaseRound.builder()
                .userId(userId)
                .mousePositions(randomPositions())
                .catPositions(randomPositions())
                .build();

        return StartCatChaseRoundResponse.from(roundRepository.save(round));
    }

    @Transactional
    public void submitJudgments(Long userId, Long roundId, SubmitCatChaseJudgmentsRequest request) {
        CatChaseRound round = getOwnedRound(userId, roundId);

        if (judgmentRepository.existsByRoundId(roundId)) {
            throw CatChaseException.alreadySubmitted();
        }

        List<SubmitCatChaseJudgmentsRequest.JudgmentItem> items = request.judgments();
        List<Integer> catPositions = round.getCatPositions();

        if (items.size() != catPositions.size()) {
            throw CatChaseException.judgmentCountMismatch();
        }
        for (SubmitCatChaseJudgmentsRequest.JudgmentItem item : items) {
            if (!catPositions.contains(item.position())) {
                throw CatChaseException.invalidPosition();
            }
        }

        List<Integer> mousePositions = round.getMousePositions();
        List<CatChaseJudgment> judgments = items.stream()
                .map(item -> CatChaseJudgment.builder()
                        .roundId(roundId)
                        .position(item.position())
                        .judgment(item.judgment())
                        .confidence(item.confidence())
                        .hadMouse(mousePositions.contains(item.position()))
                        .build())
                .toList();

        judgmentRepository.saveAll(judgments);
    }

    @Transactional(readOnly = true)
    public CatChaseRoundDetailResponse getRound(Long userId, Long roundId) {
        CatChaseRound round = getOwnedRound(userId, roundId);
        List<CatChaseJudgment> judgments = judgmentRepository.findAllByRoundId(roundId);
        return CatChaseRoundDetailResponse.from(round, judgments);
    }

    @Transactional(readOnly = true)
    public List<CatChaseRoundSummaryResponse> getMyRounds(Long userId) {
        return roundRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(round -> CatChaseRoundSummaryResponse.from(round, judgmentRepository.existsByRoundId(round.getId())))
                .toList();
    }

    private CatChaseRound getOwnedRound(Long userId, Long roundId) {
        CatChaseRound round = roundRepository.findById(roundId)
                .orElseThrow(CatChaseException::notFound);
        if (!round.isOwnedBy(userId)) {
            throw CatChaseException.notFound();
        }
        return round;
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
