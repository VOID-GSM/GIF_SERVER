package com.example.gifserverv2.domain.student.controller;

import com.example.gifserverv2.domain.student.dto.request.AssignTeamRequest;
import com.example.gifserverv2.domain.student.dto.request.ChangeTeamRoleRequest;
import com.example.gifserverv2.domain.student.dto.response.RosterUploadResponse;
import com.example.gifserverv2.domain.student.dto.response.StudentResponse;
import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.dto.response.UnregisteredStudentResponse;
import com.example.gifserverv2.domain.student.service.AdminStudentService;
import com.example.gifserverv2.domain.student.service.StudentRosterService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    private final StudentRosterService studentRosterService;

    @Operation(summary = "학생 목록·검색", description = "keyword: 이름·학번 부분 일치, assigned=false 면 미배정 학생만")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Boolean assigned) {
        return ResponseEntity.ok(adminStudentService.getStudents(keyword, grade, assigned));
    }

    @Operation(summary = "미배정 학생 목록", description = "어느 팀에도 속하지 않은 학생")
    @GetMapping("/unassigned")
    public ResponseEntity<List<StudentResponse>> getUnassignedStudents(
            @RequestParam(required = false) String grade) {
        return ResponseEntity.ok(adminStudentService.getStudents(null, grade, false));
    }

    @Operation(summary = "미가입 학생 목록 (Master·Void)", description = "업로드한 학생 명단 중 아직 회원가입하지 않은 학생")
    @GetMapping("/unregistered")
    public ResponseEntity<List<UnregisteredStudentResponse>> getUnregisteredStudents(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Integer grade) {
        return ResponseEntity.ok(studentRosterService.getUnregistered(currentUser, grade));
    }

    @Operation(summary = "학생 명단 업로드 (Master·Void)", description = "\"학번,이름\" 형식 CSV. 이미 있는 학번은 이름을 갱신한다.")
    @PostMapping(value = "/roster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RosterUploadResponse> uploadRoster(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentRosterService.upload(currentUser, file));
    }

    @Operation(summary = "미배정 학생 팀 배정 요청 (Master·Void)", description = "해당 프로젝트 팀장이 수락하면 팀원으로 추가된다.")
    @PostMapping("/{userId}/team-requests")
    public ResponseEntity<TeamJoinRequestResponse> requestAssign(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody AssignTeamRequest request) {
        return ResponseEntity.ok(adminStudentService.requestAssign(currentUser, userId, request.projectId()));
    }

    @Operation(summary = "승인 대기 중인 팀 배정 요청 목록 (Master·Void)")
    @GetMapping("/team-requests")
    public ResponseEntity<List<TeamJoinRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(adminStudentService.getPendingRequests(currentUser));
    }

    @Operation(summary = "팀 배정 요청 취소 (Master·Void)")
    @DeleteMapping("/team-requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long requestId) {
        adminStudentService.cancelRequest(currentUser, requestId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 이동 (Master·Void)", description = "이미 팀에 속한 학생을 다른 팀 팀원으로 옮긴다. 팀장은 옮길 수 없다.")
    @PatchMapping("/{userId}/team")
    public ResponseEntity<Void> moveTeam(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody AssignTeamRequest request) {
        adminStudentService.moveTeam(currentUser, userId, request.projectId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "팀에서 제외 (Master·Void)", description = "팀장은 제외할 수 없다.")
    @DeleteMapping("/{userId}/team")
    public ResponseEntity<Void> removeFromTeam(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @RequestParam Long projectId) {
        adminStudentService.removeFromTeam(currentUser, userId, projectId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 내 역할 변경 (Master)", description = "LEADER 로 지정하면 기존 팀장은 팀원이 된다. 기존 팀장 양도와 같은 동작이다.")
    @PatchMapping("/{userId}/team-role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long userId,
            @Valid @RequestBody ChangeTeamRoleRequest request) {
        adminStudentService.changeRole(currentUser, userId, request.projectId(), request.role());
        return ResponseEntity.ok().build();
    }
}
