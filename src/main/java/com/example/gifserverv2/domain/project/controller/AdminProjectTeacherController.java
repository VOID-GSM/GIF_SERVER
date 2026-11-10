package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.AssignProjectTeacherRequest;
import com.example.gifserverv2.domain.project.dto.response.TeacherListResponse;
import com.example.gifserverv2.domain.project.service.AdminTeacherManagementService;
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

@Tag(name = "담당 교사 배정 (MASTER)", description = "MASTER 선생님이 프로젝트 담당 교사를 지정한다. 지정된 선생님이 수락하면 담당 교사가 된다.")
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminProjectTeacherController {

    private final AdminTeacherManagementService adminTeacherManagementService;

    @Operation(summary = "선생님 목록과 배정·채점 현황 (MASTER)", description = """
            MASTER 선생님만 호출할 수 있다. 모든 선생님과 각자의 최근 담당 교사 배정 상태, 채점하지 않은 프로젝트 목록을 반환한다.
            `projectId`를 주면 `assignmentInfo`는 해당 프로젝트에 대한 최근 배정 요청 기준이다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "MASTER 선생님이 아님")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherListResponse>> getAllTeachers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "배정 상태를 볼 프로젝트 ID (선택)", example = "1") @RequestParam(name = "projectId", required = false) Long projectId) {

        List<TeacherListResponse> response = adminTeacherManagementService.getAllTeachers(currentUser, projectId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로젝트 담당 교사 지정 요청 (MASTER)", description = """
            MASTER 선생님만 호출할 수 있다. 지정한 선생님에게 담당 교사 요청(PENDING)을 보낸다.
            MASTER 본인을 지정하면 요청 없이 바로 담당 교사가 된다(ACCEPTED).""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락, 대상이 선생님 권한이 없음"),
            @ApiResponse(responseCode = "403", description = "MASTER 선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "프로젝트 또는 선생님을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "같은 선생님에게 이미 대기 중인 요청이 있음")
    })
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignTeacher(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AssignProjectTeacherRequest request) {

        adminTeacherManagementService.assignTeacherToProject(currentUser, request);
        return ResponseEntity.ok().build();
    }
}