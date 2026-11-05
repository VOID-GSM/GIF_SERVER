package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.response.AdminCommentPageResponse;
import com.example.gifserverv2.domain.engagement.service.AdminCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 관리자가 아니면 403
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    public ResponseEntity<AdminCommentPageResponse> getComments(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminCommentService.getComments(projectId, userId, page, size));
    }

    @PatchMapping("/{commentId}/hide")
    public ResponseEntity<Void> hide(@PathVariable Long commentId) {
        adminCommentService.hide(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{commentId}/unhide")
    public ResponseEntity<Void> unhide(@PathVariable Long commentId) {
        adminCommentService.unhide(commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable Long commentId) {
        adminCommentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}