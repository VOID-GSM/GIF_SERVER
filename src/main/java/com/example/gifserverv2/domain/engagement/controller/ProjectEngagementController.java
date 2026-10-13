package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.*;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project/{projectId}")
@RequiredArgsConstructor
public class ProjectEngagementController {

    private final ProjectEngagementService engagementService;

    @GetMapping("/engagement")
    public ResponseEntity<EngagementResponse> getEngagement(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.getEngagement(projectId, user.userId()));
    }

    @GetMapping("/comments")
    public ResponseEntity<CommentPageResponse> getComments(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(engagementService.getComments(projectId, page, size));
    }

    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(engagementService.createComment(projectId, user.userId(), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @PathVariable Long commentId
    ) {
        engagementService.deleteComment(projectId, commentId, user.userId(), user.role() == Role.ADMIN);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.toggleLike(projectId, user.userId()));
    }

    @DeleteMapping("/like")
    public ResponseEntity<LikeResponse> unlike(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.unlike(projectId, user.userId()));
    }

    @PostMapping("/view")
    public ResponseEntity<ViewResponse> increaseView(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.increaseView(projectId, user.userId()));
    }
}
