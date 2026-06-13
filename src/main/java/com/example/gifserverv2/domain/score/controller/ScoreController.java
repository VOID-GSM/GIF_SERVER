package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.CreateMajorScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateReportScoreRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateSocialScoreRequest;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.score.service.MajorScoreService;
import com.example.gifserverv2.domain.score.service.ReportScoreService;
import com.example.gifserverv2.domain.score.service.SocialScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gifserverv2.domain.score.dto.response.DetailScoreResponse;

@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final MajorScoreService majorScoreService;
    private final ReportScoreService reportScoreService;
    private final SocialScoreService socialScoreService;

    private String evaluatorId(AuthenticatedUser user) {
        return user.userId().toString();
    }

    @PostMapping("/major")
    public ResponseEntity<Void> createMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestBody CreateMajorScoreRequest request) {
        majorScoreService.createMajor(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/major")
    public ResponseEntity<Void> updateMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestBody CreateMajorScoreRequest request) {
        majorScoreService.updateMajor(request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/major")
    public ResponseEntity<DetailScoreResponse> getMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @RequestParam Long projectId) {
        var score = majorScoreService.getMajor(projectId, user);
        return ResponseEntity.ok(new DetailScoreResponse(score));
    }

    @PostMapping("/report")
    public ResponseEntity<Void> createReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateReportScoreRequest request) {
        reportScoreService.createReport(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/report")
    public ResponseEntity<Void> updateReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateReportScoreRequest request) {
        reportScoreService.updateReport(request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    public ResponseEntity<DetailScoreResponse> getReport(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @RequestParam Long projectId) {
        var score = reportScoreService.getReport(projectId, user);
        return ResponseEntity.ok(new DetailScoreResponse(score));
    }

    @PostMapping("/social")
    public ResponseEntity<Void> createSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateSocialScoreRequest request) {
        socialScoreService.createSocial(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/social")
    public ResponseEntity<Void> updateSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateSocialScoreRequest request) {
        socialScoreService.updateSocial(request, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/social")
    public ResponseEntity<DetailScoreResponse> getSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @RequestParam Long projectId) {
        var score = socialScoreService.getSocial(projectId, user);
        return ResponseEntity.ok(new DetailScoreResponse(score));
    }
}
