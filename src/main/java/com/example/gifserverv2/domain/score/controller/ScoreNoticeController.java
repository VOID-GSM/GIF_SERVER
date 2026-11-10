package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.response.GetScoreNoticeResponse;
import com.example.gifserverv2.domain.score.service.ScoreNoticeService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "성적 공지", description = "MASTER가 현재 점수로 성적을 공지(스냅샷 저장)하면 학생·선생님이 공지된 결과를 조회한다.")
@RestController
@RequestMapping("/api/score/notice")
public class ScoreNoticeController {

    private final ScoreNoticeService scoreNoticeService;

    public ScoreNoticeController(ScoreNoticeService scoreNoticeService) {
        this.scoreNoticeService = scoreNoticeService;
    }

    @Operation(summary = "성적 공지 (MASTER)", description = "현재 점수로 프로젝트별 평균 점수 스냅샷을 저장하고 모든 사용자에게 알림을 보낸다. 다시 호출하면 새 공지가 생기고 조회 시 가장 최근 공지가 보인다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공지 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER가 아님"),
            @ApiResponse(responseCode = "500", description = "공지 저장 실패")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> publish(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        scoreNoticeService.publish(currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "최근 성적 공지 조회", description = "학생·선생님 모두 호출할 수 있다. 공지가 없으면 `isPublished=false`, `scores=[]`를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<GetScoreNoticeResponse> getCurrent() {
        return ResponseEntity.ok(scoreNoticeService.getCurrentNotice());
    }
}
