package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.RespondAssignmentRequest;
import com.example.gifserverv2.domain.project.dto.response.MyTeacherAssignmentResponse;
import com.example.gifserverv2.domain.project.service.TeacherAssignmentService;
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

@Tag(name = "담당 교사 요청 응답", description = "선생님이 자신에게 온 프로젝트 담당 교사 요청을 확인하고 수락·거절한다.")
@RestController
@RequestMapping("/api/teachers/assignments")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    @Operation(summary = "내게 온 담당 교사 요청 목록 (선생님)", description = "선생님만 호출할 수 있다. 상태(PENDING/ACCEPTED/REJECTED)와 관계없이 최신순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MyTeacherAssignmentResponse>> getMyAssignments(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<MyTeacherAssignmentResponse> response = teacherAssignmentService.getMyAssignments(currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "담당 교사 요청 수락·거절 (선생님)", description = """
            본인에게 온 요청만 처리할 수 있다.
            - `ACCEPTED`: 해당 프로젝트의 담당 교사가 되고, 내 담당 팀(`adminTeam`)이 그 팀명으로 바뀐다.
            - `REJECTED`: `rejectReason`(최대 200자)이 필수다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공"),
            @ApiResponse(responseCode = "400", description = "status 누락·잘못된 값, 거절 사유 누락 또는 200자 초과"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 본인의 요청이 아님"),
            @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    @PatchMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> respondAssignment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "배정 요청 ID", example = "5") @PathVariable Long assignmentId,
            @Valid @RequestBody RespondAssignmentRequest request) {

        teacherAssignmentService.respondToAssignment(assignmentId, request, currentUser);
        return ResponseEntity.ok().build();
    }
}