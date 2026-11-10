package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.*;
import com.example.gifserverv2.domain.engagement.service.ProjectEngagementService;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gifserverv2.domain.engagement.service.AnonymousViewService;
import jakarta.servlet.http.HttpServletRequest;


@Tag(name = "프로젝트 반응", description = """
        프로젝트 좋아요·댓글·조회수.
        외부 사용자(`role=EXTERNAL`)도 이 태그의 API를 모두 사용할 수 있다. 댓글 목록과 조회수 증가는 로그인 없이도 호출할 수 있다.""")
@RestController
@RequestMapping("/api/project/{projectId}")
@RequiredArgsConstructor
public class ProjectEngagementController {

    private final AnonymousViewService anonymousViewService;
    private final ProjectEngagementService engagementService;

    @Operation(summary = "프로젝트 반응 요약", description = "로그인한 사용자(외부 사용자 포함)가 호출할 수 있다. 조회수·좋아요 수·댓글 수와 내 좋아요 여부를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/engagement")
    public ResponseEntity<EngagementResponse> getEngagement(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.getEngagement(projectId, user.userId()));
    }

    @Operation(summary = "댓글 목록", description = "로그인 없이 호출할 수 있다(토큰은 선택). 최신순이며 숨김·삭제된 댓글은 제외된다. 비로그인이면 `isMine`은 항상 false다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/comments")
    public ResponseEntity<CommentPageResponse> getComments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "페이지 번호 (1부터). 1보다 작으면 1로 처리", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (1~50, 기본 20). 범위를 벗어나면 가까운 값으로 보정", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        // 비로그인이면 user 가 null
        Long userId = user == null ? null : user.userId();
        return ResponseEntity.ok(engagementService.getComments(projectId, userId, page, size));
    }

    @Operation(summary = "댓글 작성", description = "로그인한 사용자(외부 사용자 포함)가 작성할 수 있다. 같은 사용자는 1분에 5개까지 작성할 수 있다. 작성된 댓글을 201로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "내용이 비었거나 500자 초과"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트"),
            @ApiResponse(responseCode = "429", description = "1분 내 작성 횟수(5회) 초과")
    })
    @PostMapping("/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(engagementService.createComment(projectId, user.userId(), request));
    }

    @Operation(summary = "댓글 삭제", description = "본인 댓글만 삭제할 수 있다. 선생님(`role=ADMIN`)은 모든 댓글을 삭제할 수 있다. 실제로 지우지 않고 삭제 처리(soft delete)한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인 댓글이 아님"),
            @ApiResponse(responseCode = "404", description = "댓글이 없거나 이미 삭제됨, 다른 프로젝트의 댓글")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "댓글 ID", example = "10") @PathVariable Long commentId
    ) {
        engagementService.deleteComment(projectId, commentId, user.userId(), user.role() == Role.ADMIN);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "좋아요 토글", description = "좋아요를 누르지 않았으면 추가하고, 이미 눌렀으면 취소한다. 처리 후 상태(`liked`)와 좋아요 수를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.toggleLike(projectId, user.userId()));
    }

    @Operation(summary = "좋아요 취소", description = "좋아요를 취소한다. 누르지 않은 상태여도 성공하며 `liked`는 항상 false다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @DeleteMapping("/like")
    public ResponseEntity<LikeResponse> unlike(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(engagementService.unlike(projectId, user.userId()));
    }

    @Operation(summary = "조회수 증가", description = """
            로그인 없이 호출할 수 있다(토큰은 선택). 프로젝트 상세 화면에 들어갈 때 한 번 호출한다.
            같은 사용자(비로그인은 같은 방문자)가 30분 안에 다시 조회하면 집계하지 않는다.
            - 로그인 사용자: userId 기준으로 중복을 거른다.
            - 비로그인: `X-Visitor-Id` 헤더 기준으로 거르고, 없거나 형식이 맞지 않으면 IP+User-Agent로 대신한다. 같은 IP에서 1분에 30회를 넘으면 집계하지 않는다.

            집계 여부와 관계없이 현재 누적 조회수를 반환한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/view")
    public ResponseEntity<ViewResponse> increaseView(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "비로그인 방문자 식별자 (선택). 영문·숫자·`_`·`-` 16~64자. 브라우저에 저장해 두고 재사용한다", example = "3f2a9c7e1b4d4e8fa0c1") @RequestHeader(value = "X-Visitor-Id", required = false) String visitorId,
            HttpServletRequest request
    ) {
        if (user != null) {
            return ResponseEntity.ok(engagementService.increaseView(projectId, user.userId()));
        }
        return ResponseEntity.ok(anonymousViewService.increaseView(
                projectId, visitorId, request.getRemoteAddr(), request.getHeader("User-Agent")));
    }
}
