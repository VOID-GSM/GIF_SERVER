package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.*;
import com.example.gifserverv2.domain.score.dto.response.GetProjectFieldAverageResponse;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.service.*;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import com.example.gifserverv2.domain.score.dto.response.GetScoreRankResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gifserverv2.domain.score.dto.response.GetDetailScoreResponse;

import java.util.List;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final MajorScoreService majorScoreService;
    private final ReportScoreService reportScoreService;
    private final SocialScoreService socialScoreService;
    private final ScoreNoticeService scoreNoticeService;
    private final ScoreQueryService scoreQueryService;

    private String evaluatorId(AuthenticatedUser user) {
        return user.userId().toString();
    }

    @PostMapping("/major")
    public ResponseEntity<Void> createMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestBody CreateMajorScoreRequest request) {
        majorScoreService.createMajor(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/major/{projectId}")
    public ResponseEntity<Void> updateMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @PathVariable("projectId") Long projectId,
                                            @RequestBody PatchMajorScoreRequest request) {
        majorScoreService.updateMajor(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/major")
    public ResponseEntity<GetDetailScoreResponse> getMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                                           @RequestParam Long projectId) {
        Score score = majorScoreService.getMajor(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @PostMapping("/report")
    public ResponseEntity<Void> createReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateReportScoreRequest request) {
        reportScoreService.createReport(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/report/{projectId}")
    public ResponseEntity<Void> updateReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable("projectId") Long projectId,
                                             @RequestBody PatchReportScoreRequest request) {
        reportScoreService.updateReport(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    public ResponseEntity<GetDetailScoreResponse> getReport(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @RequestParam Long projectId) {
        Score score = reportScoreService.getReport(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @PostMapping("/social")
    public ResponseEntity<Void> createSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateSocialScoreRequest request) {
        socialScoreService.createSocial(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/social/{projectId}")
    public ResponseEntity<Void> updateSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable("projectId") Long projectId,
                                             @RequestBody PatchSocialScoreRequest request) {
        socialScoreService.updateSocial(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/social")
    public ResponseEntity<GetDetailScoreResponse> getSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @RequestParam Long projectId) {
        Score score = socialScoreService.getSocial(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @GetMapping("/projects/averages")
    public ResponseEntity<List<GetProjectFieldAverageResponse>> getAllProjectFieldAverages(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(scoreQueryService.getAllProjectFieldAverages(user));
    }

    @GetMapping("/projects/{projectId}/averages")
    public ResponseEntity<GetProjectFieldAverageResponse> getProjectFieldAverages(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable("projectId") Long projectId) {

        GetProjectFieldAverageResponse response = scoreQueryService.getProjectFieldAverages(projectId, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rank")
    public ResponseEntity<List<GetScoreRankResponse>> getRank(
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer rank) {

        return ResponseEntity.ok(scoreNoticeService.getRankByGradeAndRank(grade, rank));
    }
}
