package com.example.gifserverv2.domain.retrospect.controller;

import com.example.gifserverv2.domain.retrospect.dto.request.WriteRetrospectRequest;
import com.example.gifserverv2.domain.retrospect.dto.response.DetailRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.dto.response.ListRetrospectResponse;
import com.example.gifserverv2.domain.retrospect.service.RetrospectService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로젝트 회고록", description = "프로젝트 팀원이 프로젝트에 대한 회고록을 한 사람당 하나씩 작성한다. 이 태그의 API는 해당 프로젝트 팀원만 호출할 수 있다.")
@RestController
@RequestMapping("/api/project/{projectId}/retrospect")
@RequiredArgsConstructor
public class RetrospectController {

    private final RetrospectService retrospectService;

    @Operation(summary = "내 회고록 작성·수정", description = "내 회고록이 없으면 만들고, 있으면 제목·내용을 덮어쓴다. 응답 본문은 회고록 ID(숫자)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공. 회고록 ID 반환"),
            @ApiResponse(responseCode = "400", description = "제목·내용 누락 또는 길이 초과"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님")
    })
    @PutMapping
    public ResponseEntity<Long> writeOrUpdate(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody WriteRetrospectRequest request
    ) {
        return ResponseEntity.ok(retrospectService.writeOrUpdate(projectId, user.userId(), request));
    }

    @Operation(summary = "내 회고록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "작성한 회고록 없음")
    })
    @GetMapping("/my")
    public ResponseEntity<DetailRetrospectResponse> getMy(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(retrospectService.getMy(projectId, user.userId()));
    }

    @Operation(summary = "팀 회고록 목록", description = "팀원들이 쓴 회고록을 최근 수정순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님")
    })
    @GetMapping
    public ResponseEntity<List<ListRetrospectResponse>> getTeamRetrospects(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(retrospectService.getTeamRetrospects(projectId, user.userId()));
    }

    @Operation(summary = "팀 회고록 상세")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "회고록이 없거나 다른 프로젝트의 회고록")
    })
    @GetMapping("/{retrospectId}")
    public ResponseEntity<DetailRetrospectResponse> getDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "회고록 ID", example = "4") @PathVariable Long retrospectId
    ) {
        return ResponseEntity.ok(retrospectService.getDetail(projectId, user.userId(), retrospectId));
    }

    @Operation(summary = "내 회고록 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "작성한 회고록 없음")
    })
    @DeleteMapping
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        retrospectService.delete(projectId, user.userId());
        return ResponseEntity.noContent().build();
    }
}
