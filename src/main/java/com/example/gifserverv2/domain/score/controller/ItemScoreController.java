package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest;
import com.example.gifserverv2.domain.score.dto.response.ProjectItemScoreResponse;
import com.example.gifserverv2.domain.score.service.ItemScoreService;
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

@Tag(name = "항목 기반 채점", description = "평가 기준(영역·항목)에 따라 선생님이 프로젝트를 채점한다. 영역별 채점(`/api/score/major` 등)과는 별개로 저장된다.")
@RestController
@RequestMapping("/api/score/projects/{projectId}/items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ItemScoreController {

    private final ItemScoreService itemScoreService;

    @Operation(summary = "내 항목별 채점 현황 (선생님)", description = "로그인한 선생님이 이 프로젝트에 매긴 항목별 점수를 영역별로 묶어 반환한다. 채점하지 않은 항목의 `score`는 null이다. VOID 계정은 호출할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님 역할이 없거나 VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping
    public ResponseEntity<ProjectItemScoreResponse> getScores(@AuthenticationPrincipal AuthenticatedUser user,
                                                              @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId) {
        return ResponseEntity.ok(itemScoreService.getScores(projectId, user));
    }

    @Operation(summary = "항목별 점수 저장 (선생님)", description = """
            요청에 담긴 항목만 저장한다(없으면 생성, 있으면 수정). 담기지 않은 항목의 기존 점수는 그대로 둔다.
            `score`는 해당 항목의 선택지(`options`) 중 하나여야 한다. 저장 후 전체 채점 현황을 반환한다. VOID 계정은 채점할 수 없다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "scores 비어 있음, 같은 항목 중복, 선택지에 없는 점수"),
            @ApiResponse(responseCode = "403", description = "선생님 역할이 없거나 VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트 또는 항목"),
            @ApiResponse(responseCode = "409", description = "같은 항목에 대한 동시 저장 충돌. 다시 시도")
    })
    @PutMapping
    public ResponseEntity<ProjectItemScoreResponse> saveScores(@AuthenticationPrincipal AuthenticatedUser user,
                                                               @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
                                                               @Valid @RequestBody SaveItemScoresRequest request) {
        return ResponseEntity.ok(itemScoreService.saveScores(projectId, user, request));
    }
}
