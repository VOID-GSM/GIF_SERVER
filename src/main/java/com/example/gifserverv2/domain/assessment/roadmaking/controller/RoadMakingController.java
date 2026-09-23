package com.example.gifserverv2.domain.assessment.roadmaking.controller;

import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.SubmitRoadMakingAnswerRequest;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingProblemResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingRoundResultResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.StartRoadMakingRoundResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.SubmitRoadMakingAnswerResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.service.RoadMakingService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment/road-making")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class RoadMakingController {
    private final RoadMakingService roadMakingService;

    @PostMapping("/rounds")
    public ResponseEntity<StartRoadMakingRoundResponse> startRound(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roadMakingService.startRound(user.userId()));
    }

    @GetMapping("/rounds/{roundId}/problems/current")
    public ResponseEntity<RoadMakingProblemResponse> getCurrentProblem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(roadMakingService.getCurrentProblem(user.userId(), roundId));
    }

    @PostMapping("/rounds/{roundId}/problems/{problemId}/submit")
    public ResponseEntity<SubmitRoadMakingAnswerResponse> submitAnswer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId,
            @PathVariable Long problemId,
            @Valid @RequestBody SubmitRoadMakingAnswerRequest request
    ) {
        return ResponseEntity.ok(roadMakingService.submit(user.userId(), roundId, problemId, request));
    }

    @PostMapping("/rounds/{roundId}/finish")
    public ResponseEntity<RoadMakingRoundResultResponse> finishRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(roadMakingService.finishRound(user.userId(), roundId));
    }
}