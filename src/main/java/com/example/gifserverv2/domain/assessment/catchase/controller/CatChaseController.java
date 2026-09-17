package com.example.gifserverv2.domain.assessment.catchase.controller;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundSummaryResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.StartCatChaseRoundResponse;
import com.example.gifserverv2.domain.assessment.catchase.service.CatChaseService;
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
    public ResponseEntity<StartCatChaseRoundResponse> startRound(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(catChaseService.startRound(user.userId()));
    }

    @PostMapping("/rounds/{roundId}/judgments")
    public ResponseEntity<Void> submitJudgments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId,
            @Valid @RequestBody SubmitCatChaseJudgmentsRequest request
    ) {
        catChaseService.submitJudgments(user.userId(), roundId, request);
        return ResponseEntity.noContent().build();
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
}
