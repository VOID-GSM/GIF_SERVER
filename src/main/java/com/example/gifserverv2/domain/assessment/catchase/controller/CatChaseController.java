package com.example.gifserverv2.domain.assessment.catchase.controller;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundResultResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundSummaryResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.StartCatChaseRoundResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.SubmitCatChaseJudgmentsResponse;
import com.example.gifserverv2.domain.assessment.catchase.service.CatChaseService;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment/cat-chase")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CatChaseController {

    private final CatChaseService catChaseService;

    @PostMapping("/rounds")
    public ResponseEntity<StartCatChaseRoundResponse> startRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartAssessmentRoundRequest request
    ) {
        return ResponseEntity.ok(catChaseService.startRound(user.userId(), request));
    }

    @GetMapping("/rounds/{roundId}/questions/current")
    public ResponseEntity<CatChaseQuestionResponse> getCurrentQuestion(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.getCurrentQuestion(user.userId(), roundId));
    }

    @PostMapping("/rounds/{roundId}/questions/{questionId}/judgments")
    public ResponseEntity<SubmitCatChaseJudgmentsResponse> submitJudgments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId,
            @PathVariable Long questionId,
            @Valid @RequestBody SubmitCatChaseJudgmentsRequest request
    ) {
        return ResponseEntity.ok(catChaseService.submitJudgments(user.userId(), roundId, questionId, request));
    }

    @PostMapping("/rounds/{roundId}/finish")
    public ResponseEntity<CatChaseRoundResultResponse> finishRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.finishRound(user.userId(), roundId));
    }

    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<CatChaseRoundDetailResponse> getRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.getRound(user.userId(), roundId));
    }

    @GetMapping("/rounds")
    public ResponseEntity<List<CatChaseRoundSummaryResponse>> getMyRounds(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(catChaseService.getMyRounds(user.userId()));
    }

    @GetMapping("/ranking")
    public ResponseEntity<AssessmentRankingResponse> getRanking(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "7") int limit
    ) {
        return ResponseEntity.ok(catChaseService.getRanking(user.userId(), limit));
    }
}
