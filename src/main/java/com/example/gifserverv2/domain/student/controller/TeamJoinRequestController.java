package com.example.gifserverv2.domain.student.controller;

import com.example.gifserverv2.domain.student.dto.request.RespondTeamJoinRequest;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.service.TeamJoinRequestService;
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

@Tag(name = "팀원 추가 요청", description = "선생님(MASTER·VOID)이 보낸 팀 배정 요청을 팀장이 확인하고 수락·거절한다.")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class TeamJoinRequestController {

    private final TeamJoinRequestService teamJoinRequestService;

    @Operation(summary = "우리 팀 팀원 추가 요청 목록 (팀장)", description = "해당 팀 팀장만 조회할 수 있다(MASTER·VOID 선생님은 모든 팀 조회 가능). 대기 중인 요청만 최신순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "팀장이 아님")
    })
    @GetMapping("/{projectId}/join-requests")
    public ResponseEntity<List<TeamJoinRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId) {
        return ResponseEntity.ok(teamJoinRequestService.getPendingRequests(currentUser, projectId));
    }

    @Operation(summary = "팀원 추가 요청 수락·거절 (팀장)", description = """
            해당 팀 팀장만 처리할 수 있다.
            - 수락: 학생이 팀원으로 추가되고 학생에게 알림이 간다. 그사이 학생이 다른 팀에 들어갔으면 요청이 취소되고 409를 응답한다.
            - 거절: `rejectReason`(선택, 최대 200자)과 함께 요청한 선생님에게 알림이 간다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "accept 누락, 거절 사유 200자 초과"),
            @ApiResponse(responseCode = "403", description = "팀장이 아님"),
            @ApiResponse(responseCode = "404", description = "요청 또는 학생 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청, 학생이 이미 다른 팀 소속(요청 취소됨)")
    })
    @PatchMapping("/join-requests/{requestId}")
    public ResponseEntity<Void> respond(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "팀원 추가 요청 ID", example = "9") @PathVariable Long requestId,
            @Valid @RequestBody RespondTeamJoinRequest request) {
        teamJoinRequestService.respond(currentUser, requestId, request.accept(), request.rejectReason());
        return ResponseEntity.ok().build();
    }
}
