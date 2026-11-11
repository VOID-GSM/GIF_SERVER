package com.example.gifserverv2.domain.assessment.catchase.controller;

import com.example.gifserverv2.domain.assessment.catchase.dto.request.SubmitCatChaseJudgmentsRequest;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseQuestionResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundDetailResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundResultResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.CatChaseRoundSummaryResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.StartCatChaseRoundResponse;
import com.example.gifserverv2.domain.assessment.catchase.dto.response.SubmitCatChaseJudgmentsResponse;
import com.example.gifserverv2.domain.assessment.catchase.service.CatChaseService;
import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
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

@Tag(name = "역량검사 - 고양이 술래잡기", description = """
        36칸 격자에 쥐(1~13마리)와 고양이(빨강·파랑 2마리) 위치가 주어지면, 고양이 칸마다 쥐가 있었는지(FOUND/MISSED)와 확신도를 판단해 제출한다.
        응답 시간을 넘긴 고양이는 TIMEOUT 으로 보내며 오답으로 처리된다.
        문항에 응답하면 다음 문항이 바로 발급된다. 학생(`role=USER`)만 호출할 수 있다. 끝까지 진행하고 1문항 이상 맞히면 20코인 + 맞힌 문항당 10코인을 받는다. 중간에 종료하거나 모두 틀리면 0코인이다.""")
@RestController
@RequestMapping("/api/assessment/cat-chase")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'EXTERNAL')")
public class CatChaseController {

    private final CatChaseService catChaseService;

    @Operation(summary = "라운드 시작", description = """
            진행 중인 라운드가 같은 문항 수(`questionCount`)면 새로 만들지 않고 그 라운드와 현재 문항을 반환한다.
            문항 수가 다르면 진행 중이던 라운드는 이탈로 종료하고(코인 없음, `/finish`와 같음) 새 라운드를 만들어 첫 문항을 함께 반환한다.
            칸 번호는 0~35이다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "시작 성공"),
            @ApiResponse(responseCode = "400", description = "문항 수가 3~10이 아님"),
            @ApiResponse(responseCode = "403", description = "학생이 아님")
    })
    @PostMapping("/rounds")
    public ResponseEntity<StartCatChaseRoundResponse> startRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartAssessmentRoundRequest request
    ) {
        return ResponseEntity.ok(catChaseService.startRound(user.userId(), request));
    }

    @Operation(summary = "현재 문항 조회", description = "새로고침 등으로 현재 풀어야 할 문항을 다시 받을 때 쓴다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 또는 문항 없음"),
            @ApiResponse(responseCode = "409", description = "이미 종료된 라운드")
    })
    @GetMapping("/rounds/{roundId}/questions/current")
    public ResponseEntity<CatChaseQuestionResponse> getCurrentQuestion(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.getCurrentQuestion(user.userId(), roundId));
    }

    @Operation(summary = "판단 제출", description = """
            문항의 고양이 칸(2개)마다 정확히 한 번씩 판단을 보낸다. 모든 판단이 맞아야 정답이다.
            응답 시간을 넘긴 칸은 `judgment: TIMEOUT`, `confidence: null` 로 보내며, 쥐 유무와 관계없이 오답이다.
            문항 발급 후 제한 시간(고양이 1마리당 10초)을 넘겨 제출하면 보낸 값과 관계없이 모든 판단을 TIMEOUT 으로 기록한다.
            남은 문항이 있으면 `nextQuestion`이, 마지막 문항이었으면 `result`(지급 코인 포함)가 채워진다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "고양이가 없는 칸에 대한 판단, 판단 개수 불일치, 값 누락"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 또는 문항 없음"),
            @ApiResponse(responseCode = "409", description = "이미 종료된 라운드, 이미 제출한 문항")
    })
    @PostMapping("/rounds/{roundId}/questions/{questionId}/judgments")
    public ResponseEntity<SubmitCatChaseJudgmentsResponse> submitJudgments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId,
            @Parameter(description = "문항 ID", example = "51") @PathVariable Long questionId,
            @Valid @RequestBody SubmitCatChaseJudgmentsRequest request
    ) {
        return ResponseEntity.ok(catChaseService.submitJudgments(user.userId(), roundId, questionId, request));
    }

    @Operation(summary = "라운드 종료", description = "진행 중인 라운드를 직접 종료한다. 모든 문항을 풀기 전에 종료하면 이탈로 처리되어 코인을 받지 않는다. 이미 끝난 라운드는 결과만 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 없음")
    })
    @PostMapping("/rounds/{roundId}/finish")
    public ResponseEntity<CatChaseRoundResultResponse> finishRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.finishRound(user.userId(), roundId));
    }

    @Operation(summary = "라운드 상세 결과", description = "문항별 정답(쥐·고양이 위치)과 내 판단을 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 없음")
    })
    @GetMapping("/rounds/{roundId}")
    public ResponseEntity<CatChaseRoundDetailResponse> getRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(catChaseService.getRound(user.userId(), roundId));
    }

    @Operation(summary = "내 라운드 목록", description = "최신순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님")
    })
    @GetMapping("/rounds")
    public ResponseEntity<List<CatChaseRoundSummaryResponse>> getMyRounds(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(catChaseService.getMyRounds(user.userId()));
    }

    @Operation(summary = "랭킹", description = """
            같은 문항 수로 끝까지 진행한 기록 중 사용자별 최고 기록으로 순위를 매긴다. 정렬: 맞힌 수 → 정답률 → 소요 시간 → 먼저 달성한 순.
            `me`는 내 순위이며 기록이 없으면 null이다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "문항 수가 3~10이 아님"),
            @ApiResponse(responseCode = "403", description = "학생이 아님")
    })
    @GetMapping("/ranking")
    public ResponseEntity<AssessmentRankingResponse> getRanking(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "문항 수 (3~10, 기본 10). 같은 문항 수로 끝까지 진행한 기록끼리 비교한다", example = "10") @RequestParam(defaultValue = "10") int questionCount,
            @Parameter(description = "상위 몇 명까지 반환할지 (1~100, 기본 7)", example = "7") @RequestParam(defaultValue = "7") int limit
    ) {
        return ResponseEntity.ok(catChaseService.getRanking(user.userId(), questionCount, limit));
    }
}
