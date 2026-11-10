package com.example.gifserverv2.domain.notice.controller;

import com.example.gifserverv2.domain.notice.dto.request.CreateNoticeRequest;
import com.example.gifserverv2.domain.notice.dto.response.DetailNoticeResponse;
import com.example.gifserverv2.domain.notice.dto.response.ListNoticeResponse;
import com.example.gifserverv2.domain.notice.service.NoticeService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "공지사항", description = "선생님이 공지를 작성하면 디스코드 채널로도 전달된다. 조회는 학생·선생님 모두 가능하다.")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 작성 (선생님)", description = "공지를 저장하고 디스코드로 알린다. `targetGrades`, `targetProjectIds`는 표시용 대상 정보이며 조회 범위를 제한하지 않는다. 응답 본문은 공지 ID(숫자)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공. 공지 ID 반환"),
            @ApiResponse(responseCode = "400", description = "제목·내용 누락 또는 길이 초과"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createNotice(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateNoticeRequest request
    ) {
        return ResponseEntity.ok(noticeService.createNotice(user.userId(), request));
    }

    @Operation(summary = "공지 목록", description = "모든 공지를 최신순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<ListNoticeResponse>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @Operation(summary = "공지 상세")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공지 없음")
    })
    @GetMapping("/{noticeId}")
    public ResponseEntity<DetailNoticeResponse> getNotice(@Parameter(description = "공지 ID", example = "5") @PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(noticeId));
    }

    @Operation(summary = "공지 삭제 (선생님)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "공지 없음")
    })
    @DeleteMapping("/{noticeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotice(@Parameter(description = "공지 ID", example = "5") @PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}
