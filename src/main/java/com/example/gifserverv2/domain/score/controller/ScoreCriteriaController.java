package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.response.ScoreAreaResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreItemResponse;
import com.example.gifserverv2.domain.score.service.ScoreCriteriaService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/score/areas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScoreCriteriaController {

    private final ScoreCriteriaService scoreCriteriaService;

    @GetMapping
    public ResponseEntity<List<ScoreAreaResponse>> getAreas() {
        return ResponseEntity.ok(scoreCriteriaService.getAreas());
    }

    @PostMapping
    public ResponseEntity<ScoreAreaResponse> createArea(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Valid @RequestBody CreateScoreAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoreCriteriaService.createArea(user, request));
    }

    @PatchMapping("/{areaId}")
    public ResponseEntity<ScoreAreaResponse> updateArea(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @PathVariable Long areaId,
                                                        @Valid @RequestBody PatchScoreAreaRequest request) {
        return ResponseEntity.ok(scoreCriteriaService.updateArea(user, areaId, request));
    }

    @DeleteMapping("/{areaId}")
    public ResponseEntity<Void> deleteArea(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long areaId) {
        scoreCriteriaService.deleteArea(user, areaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{areaId}/items")
    public ResponseEntity<ScoreItemResponse> createItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @PathVariable Long areaId,
                                                        @Valid @RequestBody CreateScoreItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoreCriteriaService.createItem(user, areaId, request));
    }

    @PatchMapping("/{areaId}/items/{itemId}")
    public ResponseEntity<ScoreItemResponse> updateItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @PathVariable Long areaId,
                                                        @PathVariable Long itemId,
                                                        @Valid @RequestBody PatchScoreItemRequest request) {
        return ResponseEntity.ok(scoreCriteriaService.updateItem(user, areaId, itemId, request));
    }

    @DeleteMapping("/{areaId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long areaId,
                                           @PathVariable Long itemId) {
        scoreCriteriaService.deleteItem(user, areaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
