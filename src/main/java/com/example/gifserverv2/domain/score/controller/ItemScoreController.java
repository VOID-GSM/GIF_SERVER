package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest;
import com.example.gifserverv2.domain.score.dto.response.ProjectItemScoreResponse;
import com.example.gifserverv2.domain.score.service.ItemScoreService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/score/projects/{projectId}/items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ItemScoreController {

    private final ItemScoreService itemScoreService;

    @GetMapping
    public ResponseEntity<ProjectItemScoreResponse> getScores(@AuthenticationPrincipal AuthenticatedUser user,
                                                              @PathVariable Long projectId) {
        return ResponseEntity.ok(itemScoreService.getScores(projectId, user));
    }

    @PutMapping
    public ResponseEntity<ProjectItemScoreResponse> saveScores(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @PathVariable Long projectId,
                                                               @Valid @RequestBody SaveItemScoresRequest request) {
        return ResponseEntity.ok(itemScoreService.saveScores(projectId, user, request));
    }
}
