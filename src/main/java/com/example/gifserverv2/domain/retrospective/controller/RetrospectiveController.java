package com.example.gifserverv2.domain.retrospective.controller;

import com.example.gifserverv2.domain.retrospective.dto.request.CreateRetrospectiveRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectivePatchRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectiveSort;
import com.example.gifserverv2.domain.retrospective.dto.response.BookmarkToggleResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.DetailRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.ListRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.service.RetrospectiveService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "활동 기록", description = """
        개인 활동 기록(회고록) 작성·조회와 북마크. 학생·선생님이 호출할 수 있다.
        본문은 마크다운이며 목록에는 본문 앞부분으로 만든 요약(최대 150자)이 내려간다. 임시 저장 기록은 `/draft` API로만 다루고 목록·상세에 나오지 않는다.""")
@RestController
@RequestMapping("/api/retrospective")
@RequiredArgsConstructor
public class RetrospectiveController {

    private final RetrospectiveService retrospectiveService;

    @Operation(summary = "활동 기록 목록", description = """
            전체 공개 기록과 내 기록(비공개 포함)을 페이지로 반환한다. `page`(0부터), `size`로 페이지를 지정한다.
            - `keyword`: 제목·본문 부분 일치
            - `bookmarkedOnly=true`: 내가 북마크한 기록만
            - `mine=true`: 내가 쓴 기록만 (마이페이지 활동 기록)""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<ListRetrospectiveResponse>> getList(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "카테고리로 거르기 (선택)", example = "TROUBLESHOOTING") @RequestParam(required = false) RetrospectiveCategory category,
            @Parameter(description = "제목·본문 검색어 (선택)", example = "Redis") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬. LATEST(최신순, 기본) / OLDEST(오래된순) / BOOKMARK(북마크 많은 순)", example = "LATEST") @RequestParam(required = false) RetrospectiveSort sort,
            @Parameter(description = "내가 북마크한 기록만", example = "false") @RequestParam(defaultValue = "false") boolean bookmarkedOnly,
            @Parameter(description = "내가 쓴 기록만", example = "false") @RequestParam(defaultValue = "false") boolean mine,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(
                retrospectiveService.getList(user.userId(), category, keyword, sort, bookmarkedOnly, mine, pageable));
    }

    @Operation(summary = "활동 기록 상세", description = "비공개 기록은 작성자만 볼 수 있다. 임시 저장 기록은 조회되지 않는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "다른 사람의 비공개 기록"),
            @ApiResponse(responseCode = "404", description = "기록이 없거나 임시 저장 기록")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DetailRetrospectiveResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "활동 기록 ID", example = "15") @PathVariable Long id
    ) {
        return ResponseEntity.ok(retrospectiveService.getDetail(user.userId(), id));
    }

    @Operation(summary = "활동 기록 작성", description = "기록을 바로 게시한다. 태그는 앞뒤 공백을 제거하고 중복을 합친다. 응답 본문은 생성된 기록 ID(숫자)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공. 기록 ID 반환"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락, 길이 초과, 태그 10개 초과, 관련 작업 형식 오류")
    })
    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateRetrospectiveRequest request
    ) {
        return ResponseEntity.ok(retrospectiveService.create(user.userId(), request));
    }

    @Operation(summary = "활동 기록 수정 (작성자)", description = "보낸 값만 바뀌고 생략한(null) 값은 유지된다. `relatedWorks`, `tags`는 보내면 전체가 교체된다(빈 배열이면 모두 삭제).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "길이 초과, 태그 10개 초과"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "기록이 없거나 임시 저장 기록")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "활동 기록 ID", example = "15") @PathVariable Long id,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        retrospectiveService.update(user.userId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "활동 기록 삭제 (작성자)", description = "기록과 이 기록에 대한 모든 북마크를 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "기록이 없거나 임시 저장 기록")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "활동 기록 ID", example = "15") @PathVariable Long id
    ) {
        retrospectiveService.delete(user.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "북마크 토글", description = "북마크하지 않았으면 추가하고, 이미 했으면 취소한다. 공개 기록만 북마크할 수 있고 내 기록은 북마크할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "내 기록"),
            @ApiResponse(responseCode = "403", description = "비공개 기록"),
            @ApiResponse(responseCode = "404", description = "기록이 없거나 임시 저장 기록")
    })
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "활동 기록 ID", example = "15") @PathVariable Long id
    ) {
        return ResponseEntity.ok(retrospectiveService.toggleBookmark(user.userId(), id));
    }

    @Operation(summary = "임시 저장", description = "모든 값이 선택이다. `visibility`를 생략하면 PRIVATE로 저장된다. 응답 본문은 임시 저장 기록 ID(숫자)다. 게시하려면 `POST /api/retrospective`로 새로 작성한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공. 기록 ID 반환"),
            @ApiResponse(responseCode = "400", description = "길이 초과, 태그 10개 초과")
    })
    @PostMapping("/draft")
    public ResponseEntity<Long> createDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        return ResponseEntity.ok(retrospectiveService.createDraft(user.userId(), request));
    }

    @Operation(summary = "임시 저장 수정 (작성자)", description = "보낸 값만 바뀌고 생략한(null) 값은 유지된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "길이 초과"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "임시 저장 기록 없음")
    })
    @PatchMapping("/draft/{id}")
    public ResponseEntity<Void> updateDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "임시 저장 기록 ID", example = "16") @PathVariable Long id,
            @Valid @RequestBody RetrospectivePatchRequest request
    ) {
        retrospectiveService.updateDraft(user.userId(), id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 임시 저장 목록", description = "최근 수정순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/draft")
    public ResponseEntity<List<DetailRetrospectiveResponse>> getMyDrafts(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(retrospectiveService.getMyDrafts(user.userId()));
    }

    @Operation(summary = "임시 저장 삭제 (작성자)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "작성자가 아님"),
            @ApiResponse(responseCode = "404", description = "임시 저장 기록 없음")
    })
    @DeleteMapping("/draft/{id}")
    public ResponseEntity<Void> deleteDraft(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "임시 저장 기록 ID", example = "16") @PathVariable Long id
    ) {
        retrospectiveService.deleteDraft(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}