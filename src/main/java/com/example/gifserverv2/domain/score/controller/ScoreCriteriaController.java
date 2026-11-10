package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.response.ScoreAreaResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreItemResponse;
import com.example.gifserverv2.domain.score.service.ScoreCriteriaService;
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

import java.util.List;

@Tag(name = "평가 기준 (영역·항목)", description = """
        항목 기반 채점에 쓰는 평가 영역과 항목(선택지 점수)을 관리한다. 이 태그의 API는 선생님만 호출할 수 있고, 생성·수정·삭제는 MASTER만 가능하다.
        삭제는 soft delete이며 기존 채점 기록은 남는다.""")
@RestController
@RequestMapping("/api/score/areas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScoreCriteriaController {

    private final ScoreCriteriaService scoreCriteriaService;

    @Operation(summary = "평가 영역·항목 목록 (선생님)", description = "삭제되지 않은 영역과 항목을 표시 순서대로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping
    public ResponseEntity<List<ScoreAreaResponse>> getAreas() {
        return ResponseEntity.ok(scoreCriteriaService.getAreas());
    }

    @Operation(summary = "평가 영역 생성 (MASTER)", description = "`displayOrder`를 생략하면 맨 뒤에 추가된다. 같은 이름의 영역은 만들 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이름 누락·100자 초과, displayOrder 음수"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "409", description = "이름 중복")
    })
    @PostMapping
    public ResponseEntity<ScoreAreaResponse> createArea(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Valid @RequestBody CreateScoreAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoreCriteriaService.createArea(user, request));
    }

    @Operation(summary = "평가 영역 수정 (MASTER)", description = "보낸 값만 바뀌고 생략한(null) 값은 유지된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "빈 이름·100자 초과, displayOrder 음수"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "영역 없음"),
            @ApiResponse(responseCode = "409", description = "이름 중복")
    })
    @PatchMapping("/{areaId}")
    public ResponseEntity<ScoreAreaResponse> updateArea(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Parameter(description = "영역 ID", example = "1") @PathVariable Long areaId,
                                                        @Valid @RequestBody PatchScoreAreaRequest request) {
        return ResponseEntity.ok(scoreCriteriaService.updateArea(user, areaId, request));
    }

    @Operation(summary = "평가 영역 삭제 (MASTER)", description = "영역과 하위 항목을 함께 삭제 처리한다. 기존 채점 기록은 남는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "영역 없음")
    })
    @DeleteMapping("/{areaId}")
    public ResponseEntity<Void> deleteArea(@AuthenticationPrincipal AuthenticatedUser user,
                                           @Parameter(description = "영역 ID", example = "1") @PathVariable Long areaId) {
        scoreCriteriaService.deleteArea(user, areaId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "평가 항목 생성 (MASTER)", description = """
            선생님은 `options`(선택지 점수) 중 하나를 골라 채점한다. 최고점(`maxScore`)은 가장 큰 선택지로 자동 계산된다.
            `options`는 1~10개, 각 0~1000점, 중복 불가, 하나 이상은 1점 이상이어야 한다. 같은 영역에 같은 이름의 항목은 만들 수 없다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이름 누락, options 규칙 위반"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "영역 없음"),
            @ApiResponse(responseCode = "409", description = "이름 중복")
    })
    @PostMapping("/{areaId}/items")
    public ResponseEntity<ScoreItemResponse> createItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Parameter(description = "영역 ID", example = "1") @PathVariable Long areaId,
                                                        @Valid @RequestBody CreateScoreItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scoreCriteriaService.createItem(user, areaId, request));
    }

    @Operation(summary = "평가 항목 수정 (MASTER)", description = "보낸 값만 바뀌고 생략한(null) 값은 유지된다. 선택지를 바꿔도 이미 기록된 점수는 바뀌지 않는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "빈 이름, options 규칙 위반"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "영역 또는 항목 없음"),
            @ApiResponse(responseCode = "409", description = "이름 중복")
    })
    @PatchMapping("/{areaId}/items/{itemId}")
    public ResponseEntity<ScoreItemResponse> updateItem(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Parameter(description = "영역 ID", example = "1") @PathVariable Long areaId,
                                                        @Parameter(description = "항목 ID", example = "4") @PathVariable Long itemId,
                                                        @Valid @RequestBody PatchScoreItemRequest request) {
        return ResponseEntity.ok(scoreCriteriaService.updateItem(user, areaId, itemId, request));
    }

    @Operation(summary = "평가 항목 삭제 (MASTER)", description = "항목을 삭제 처리한다. 기존 채점 기록은 남는다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아님"),
            @ApiResponse(responseCode = "404", description = "영역 또는 항목 없음")
    })
    @DeleteMapping("/{areaId}/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@AuthenticationPrincipal AuthenticatedUser user,
                                           @Parameter(description = "영역 ID", example = "1") @PathVariable Long areaId,
                                           @Parameter(description = "항목 ID", example = "4") @PathVariable Long itemId) {
        scoreCriteriaService.deleteItem(user, areaId, itemId);
        return ResponseEntity.noContent().build();
    }
}
