package com.example.gifserverv2.domain.assessment.roadmaking.controller;

import com.example.gifserverv2.domain.assessment.common.dto.StartAssessmentRoundRequest;
import com.example.gifserverv2.domain.assessment.ranking.dto.AssessmentRankingResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.request.SubmitRoadMakingAnswerRequest;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingProblemResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.RoadMakingRoundResultResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.StartRoadMakingRoundResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.dto.response.SubmitRoadMakingAnswerResponse;
import com.example.gifserverv2.domain.assessment.roadmaking.service.RoadMakingService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "역량검사 - 길 만들기", description = """
        격자에 차량(위치·방향)과 같은 색 사람이 주어지면, 정확히 `minFenceCount`개의 울타리를 놓아 모든 차량이 같은 색 사람에게 도착하게 한다.
        라운드 제한 시간은 5분이다. 문항마다 한 번만 응답할 수 있고, 맞히든 틀리든 다음 문항으로 넘어간다. 학생(`role=USER`)만 호출할 수 있다. 끝까지 진행하고 1문항 이상 맞히면 20코인 + 맞힌 문항당 10코인을 받는다. 중간에 종료하거나 모두 틀리면 0코인이다.""")
@RestController
@RequestMapping("/api/assessment/road-making")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class RoadMakingController {
    private final RoadMakingService roadMakingService;

    @Operation(summary = "라운드 시작", description = """
            진행 중이고 시간이 남은 라운드가 같은 문항 수(`questionCount`)면 새로 만들지 않고 그 라운드와 현재 문항을 반환한다.
            문항 수가 다르면 진행 중이던 라운드는 이탈로 종료하고(코인 없음, `/finish`와 같음) 새 문항 수로 새 라운드를 시작한다.
            시간이 지난 라운드는 정리한 뒤 새로 시작한다.
            `questionCount`만큼 응답하면 마지막 제출 응답에 `nextProblem=null`과 `result`가 담겨 라운드가 끝난다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "시작 성공"),
            @ApiResponse(responseCode = "400", description = "문항 수가 3~10이 아님"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "500", description = "문제 생성 실패")
    })
    @PostMapping("/rounds")
    public ResponseEntity<StartRoadMakingRoundResponse> startRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody StartAssessmentRoundRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roadMakingService.startRound(user.userId(), request));
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
        return ResponseEntity.ok(roadMakingService.getRanking(user.userId(), questionCount, limit));
    }

    @Operation(summary = "현재 문항 조회", description = "제한 시간이 지났으면 라운드를 종료 처리하고 409를 응답한다(1문항 이상 응답했으면 완료, 아니면 이탈).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 또는 문항 없음"),
            @ApiResponse(responseCode = "409", description = "이미 종료된 라운드, 제한 시간 초과")
    })
    @GetMapping("/rounds/{roundId}/problems/current")
    public ResponseEntity<RoadMakingProblemResponse> getCurrentProblem(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(roadMakingService.getCurrentProblem(user.userId(), roundId));
    }

    @Operation(summary = "울타리 배치 제출", description = """
            정확히 `minFenceCount`개의 울타리 위치를 보낸다. 격자 밖, 차량·사람이 있는 칸, 중복 위치에는 놓을 수 없다.
            차량별 이동 경로(`vehicleResults`)와 정답 여부를 반환한다. 남은 문항이 있으면 `nextProblem`이, 마지막이면 `result`가 채워진다.
            제한 시간(2초 여유)이 지난 뒤 제출하면 채점하지 않고 `timeOver=true`와 라운드 결과를 반환한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공"),
            @ApiResponse(responseCode = "400", description = "울타리 개수 불일치, 놓을 수 없는 위치"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 또는 문항 없음"),
            @ApiResponse(responseCode = "409", description = "이미 종료된 라운드, 이미 응답한 문항")
    })
    @PostMapping("/rounds/{roundId}/problems/{problemId}/submit")
    public ResponseEntity<SubmitRoadMakingAnswerResponse> submitAnswer(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId,
            @Parameter(description = "문항 ID", example = "71") @PathVariable Long problemId,
            @Valid @RequestBody SubmitRoadMakingAnswerRequest request
    ) {
        return ResponseEntity.ok(roadMakingService.submit(user.userId(), roundId, problemId, request));
    }

    @Operation(summary = "라운드 종료", description = "진행 중인 라운드를 직접 종료한다. 모든 문항을 풀기 전에 종료하면 이탈로 처리되어 코인을 받지 않는다. 이미 끝난 라운드는 결과만 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 성공"),
            @ApiResponse(responseCode = "403", description = "학생이 아님"),
            @ApiResponse(responseCode = "404", description = "라운드 없음")
    })
    @PostMapping("/rounds/{roundId}/finish")
    public ResponseEntity<RoadMakingRoundResultResponse> finishRound(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "라운드 ID", example = "11") @PathVariable Long roundId
    ) {
        return ResponseEntity.ok(roadMakingService.finishRound(user.userId(), roundId));
    }
}