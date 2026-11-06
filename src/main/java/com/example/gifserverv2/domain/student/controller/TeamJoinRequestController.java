package com.example.gifserverv2.domain.student.controller;

import com.example.gifserverv2.domain.student.dto.request.RespondTeamJoinRequest;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.service.TeamJoinRequestService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class TeamJoinRequestController {

    private final TeamJoinRequestService teamJoinRequestService;

    @Operation(summary = "우리 팀 팀원 추가 요청 목록 (팀장)")
    @GetMapping("/{projectId}/join-requests")
    public ResponseEntity<List<TeamJoinRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(teamJoinRequestService.getPendingRequests(currentUser, projectId));
    }

    @Operation(summary = "팀원 추가 요청 수락/거절 (팀장)")
    @PatchMapping("/join-requests/{requestId}")
    public ResponseEntity<Void> respond(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long requestId,
            @Valid @RequestBody RespondTeamJoinRequest request) {
        teamJoinRequestService.respond(currentUser, requestId, request.accept(), request.rejectReason());
        return ResponseEntity.ok().build();
    }
}
