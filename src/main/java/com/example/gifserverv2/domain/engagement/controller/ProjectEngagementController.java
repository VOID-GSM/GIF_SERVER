package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.*;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        return ResponseEntity.ok(engagementService.getEngagement(projectId, user == null ? null : user.userId()));
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
        return ResponseEntity.ok(engagementService.createComment(projectId, user.userId(), request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            Authentication authentication,
            @PathVariable Long projectId,
            @PathVariable Long commentId
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        engagementService.deleteComment(projectId, commentId, user.userId(), isAdmin);
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
            @PathVariable Long projectId,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(engagementService.increaseView(projectId, viewerKey(user, httpRequest)));
    }

    private String viewerKey(AuthenticatedUser user, HttpServletRequest request) {
        if (user != null) return "u:" + user.userId();
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "ip:" + ip;
    }
}