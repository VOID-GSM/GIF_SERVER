package com.example.gifserverv2.domain.engagement.controller;

import com.example.gifserverv2.domain.engagement.dto.response.MyCommentPageResponse;
import com.example.gifserverv2.domain.engagement.dto.response.MyLikedProjectPageResponse;
import com.example.gifserverv2.domain.engagement.service.MyEngagementService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "마이페이지")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MyEngagementController {

    private final MyEngagementService myEngagementService;

    @Operation(summary = "내가 쓴 댓글 목록", description = """
            내가 프로젝트에 쓴 댓글을 최신순(작성 시각 → ID 역순)으로 준다. 외부 사용자도 호출할 수 있다.
            삭제한 댓글은 제외한다. 관리자가 숨긴 댓글은 `hidden: true`로 포함한다(프로젝트 댓글 목록에는 보이지 않는다).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음")
    })
    @GetMapping("/comments")
    public MyCommentPageResponse getMyComments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "페이지 번호 (1부터). 1보다 작으면 1로 처리", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (1~50, 기본 20). 범위를 벗어나면 가까운 값으로 보정", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return myEngagementService.getMyComments(requireUserId(user), page, size);
    }

    @Operation(summary = "좋아요한 프로젝트 목록", description = "내가 좋아요를 누른 프로젝트를 좋아요 누른 최신순으로 준다. 외부 사용자도 호출할 수 있다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음")
    })
    @GetMapping("/likes")
    public MyLikedProjectPageResponse getMyLikes(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "페이지 번호 (1부터). 1보다 작으면 1로 처리", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (1~50, 기본 20). 범위를 벗어나면 가까운 값으로 보정", example = "20") @RequestParam(defaultValue = "20") int size
    ) {
        return myEngagementService.getMyLikes(requireUserId(user), page, size);
    }

    private static Long requireUserId(AuthenticatedUser user) {
        if (user == null || user.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
        return user.userId();
    }
}
