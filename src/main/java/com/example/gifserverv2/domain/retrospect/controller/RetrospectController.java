package com.example.gifserverv2.domain.retrospect.controller;

import com.example.gifserverv2.domain.retrospect.dto.request.WriteRetrospectRequest;
import com.example.gifserverv2.domain.retrospect.dto.response.DetailRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.dto.response.ListRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.service.RetrospectService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project/{projectId}/retrospect")
@RequiredArgsConstructor
public class RetrospectController {

    private final RetrospectService retrospectService;

    @PutMapping
    public ResponseEntity<Long> writeOrUpdate(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @RequestBody WriteRetrospectRequest request
    ) {
        return ResponseEntity.ok(retrospectService.writeOrUpdate(projectId, user.userId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<DetailRetrospectResponse> getMy(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(retrospectService.getMy(projectId, user.userId()));
    }

    @GetMapping
    public ResponseEntity<List<ListRetrospectResponse>> getTeamRetrospects(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(retrospectService.getTeamRetrospects(projectId, user.userId()));
    }

    @GetMapping("/{retrospectId}")
    public ResponseEntity<DetailRetrospectResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @PathVariable Long retrospectId
    ) {
        return ResponseEntity.ok(retrospectService.getDetail(projectId, user.userId(), retrospectId));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        retrospectService.delete(projectId, user.userId());
        return ResponseEntity.noContent().build();
    }
}