package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.response.AdminCommentPageResponse;
import com.example.gifserverv2.domain.engagement.service.AdminCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "댓글 관리 (선생님)", description = "선생님이 모든 프로젝트의 댓글을 조회하고 숨김·삭제한다. 이 태그의 API는 선생님(`role=ADMIN`)만 호출할 수 있다.")
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 관리자가 아니면 403
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @Operation(summary = "전체 댓글 목록 (선생님)", description = "숨김 댓글을 포함하고 삭제된 댓글은 제외한다. 최신순. `projectId`, `userId`를 생략하면 해당 조건으로 거르지 않는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping
    public ResponseEntity<AdminCommentPageResponse> getComments(
            @Parameter(description = "프로젝트 ID로 거르기 (선택)", example = "1") @RequestParam(required = false) Long projectId,
            @Parameter(description = "작성자 userId로 거르기 (선택)", example = "12") @RequestParam(required = false) Long userId,
            @Parameter(description = "페이지 번호 (1부터). 1보다 작으면 1로 처리", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (1~50, 기본 20). 범위를 벗어나면 가까운 값으로 보정", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(adminCommentService.getComments(projectId, userId, page, size));
    }

    @Operation(summary = "댓글 숨김 (선생님)", description = "숨긴 댓글은 일반 댓글 목록과 댓글 수에서 제외된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "숨김 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 없거나 삭제됨")
    })
    @PatchMapping("/{commentId}/hide")
    public ResponseEntity<Void> hide(@Parameter(description = "댓글 ID", example = "10") @PathVariable Long commentId) {
        adminCommentService.hide(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 숨김 해제 (선생님)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "숨김 해제 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 없거나 삭제됨")
    })
    @PatchMapping("/{commentId}/unhide")
    public ResponseEntity<Void> unhide(@Parameter(description = "댓글 ID", example = "10") @PathVariable Long commentId) {
        adminCommentService.unhide(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 삭제 (선생님)", description = "실제로 지우지 않고 삭제 처리(soft delete)한다. 삭제된 댓글은 관리자 목록에서도 보이지 않는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 없거나 이미 삭제됨")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@Parameter(description = "댓글 ID", example = "10") @PathVariable Long commentId) {
        adminCommentService.delete(commentId);
        return ResponseEntity.noContent().build();
    }
}