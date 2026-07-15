package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.response.GetScoreNoticeResponse;
import com.example.gifserverv2.domain.score.service.ScoreNoticeService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score/notice")
public class ScoreNoticeController {

    private final ScoreNoticeService scoreNoticeService;

    public ScoreNoticeController(ScoreNoticeService scoreNoticeService) {
        this.scoreNoticeService = scoreNoticeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publish(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        scoreNoticeService.publish(currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<GetScoreNoticeResponse> getCurrent() {
        return ResponseEntity.ok(scoreNoticeService.getCurrentNotice());
    }
}
