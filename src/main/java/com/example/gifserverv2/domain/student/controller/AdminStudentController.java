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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "학생 관리", description = """
        선생님이 학생 명단과 팀 배정을 관리한다. 이 태그의 API는 선생님만 호출할 수 있고, (MASTER·VOID) 표기가 있는 API는 `adminRole`이 MASTER 또는 VOID여야 한다.
        팀 배정은 바로 반영되지 않고 해당 팀 팀장에게 요청이 가며, 팀장이 수락해야 팀원이 된다.""")
@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStudentController {

    private final AdminStudentService adminStudentService;
    private final StudentRosterService studentRosterService;

    @Operation(summary = "학생 목록·검색 (선생님)", description = "가입한 학생 목록을 소속 팀·대기 중인 배정 요청과 함께 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getStudents(
            @Parameter(description = "이름·학번 부분 일치 (선택)", example = "홍길") @RequestParam(required = false) String keyword,
            @Parameter(description = "학년 (선택)", example = "2") @RequestParam(required = false) String grade,
            @Parameter(description = "true: 팀 소속 학생만, false: 미배정 학생만, 생략: 전체", example = "false") @RequestParam(required = false) Boolean assigned) {
        return ResponseEntity.ok(adminStudentService.getStudents(keyword, grade, assigned));
    }

    @Operation(summary = "미배정 학생 목록 (선생님)", description = "어느 팀에도 속하지 않은 학생. `GET /api/admin/students?assigned=false`와 같다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/unassigned")
    public ResponseEntity<List<StudentResponse>> getUnassignedStudents(
            @Parameter(description = "학년 (선택)", example = "2") @RequestParam(required = false) String grade) {
        return ResponseEntity.ok(adminStudentService.getStudents(null, grade, false));
    }

    @Operation(summary = "미가입 학생 목록 (MASTER·VOID)", description = "업로드한 학생 명단 중 아직 DataGSM으로 가입하지 않은 학생을 학번순으로 반환한다. 학번으로 비교한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님")
    })
    @GetMapping("/unregistered")
    public ResponseEntity<List<UnregisteredStudentResponse>> getUnregisteredStudents(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학년 (선택)", example = "1") @RequestParam(required = false) Integer grade) {
        return ResponseEntity.ok(studentRosterService.getUnregistered(currentUser, grade));
    }

    @Operation(summary = "학생 명단 업로드 (MASTER·VOID)", description = """
            `multipart/form-data`의 `file` 파트로 `학번,이름` 형식 CSV를 보낸다. UTF-8과 엑셀 기본 인코딩(MS949)을 모두 읽는다. 첫 줄이 머리글이면 건너뛴다.
            학번은 4자리(학년 1~3, 반 1~9, 번호 2자리)여야 한다. 이미 있는 학번은 이름을 갱신한다.
            잘못된 줄은 건너뛰고 `errors`에 줄 번호와 사유를 담아 반환한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 처리 완료 (일부 줄 오류는 errors로 반환)"),
            @ApiResponse(responseCode = "400", description = "빈 파일, 읽을 수 없는 파일"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님")
    })
    @PostMapping(value = "/roster", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RosterUploadResponse> uploadRoster(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학생 명단 CSV 파일") @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentRosterService.upload(currentUser, file));
    }

    @Operation(summary = "미배정 학생 팀 배정 요청 (MASTER·VOID)", description = "미배정 학생을 지정한 팀에 넣어 달라는 요청을 팀장에게 보낸다(알림 발송). 팀장이 수락하면 팀원으로 추가된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공"),
            @ApiResponse(responseCode = "400", description = "projectId 누락, 팀장이 없는 프로젝트"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님"),
            @ApiResponse(responseCode = "404", description = "학생 또는 프로젝트 없음"),
            @ApiResponse(responseCode = "409", description = "이미 다른 팀 소속, 이미 대기 중인 요청 있음")
    })
    @PostMapping("/{userId}/team-requests")
    public ResponseEntity<TeamJoinRequestResponse> requestAssign(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학생 userId", example = "12") @PathVariable Long userId,
            @Valid @RequestBody AssignTeamRequest request) {
        return ResponseEntity.ok(adminStudentService.requestAssign(currentUser, userId, request.projectId()));
    }

    @Operation(summary = "대기 중인 팀 배정 요청 목록 (MASTER·VOID)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님")
    })
    @GetMapping("/team-requests")
    public ResponseEntity<List<TeamJoinRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        return ResponseEntity.ok(adminStudentService.getPendingRequests(currentUser));
    }

    @Operation(summary = "팀 배정 요청 취소 (MASTER·VOID)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님"),
            @ApiResponse(responseCode = "404", description = "요청 없음"),
            @ApiResponse(responseCode = "409", description = "이미 처리된 요청")
    })
    @DeleteMapping("/team-requests/{requestId}")
    public ResponseEntity<Void> cancelRequest(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "팀원 추가 요청 ID", example = "9") @PathVariable Long requestId) {
        adminStudentService.cancelRequest(currentUser, requestId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 이동 (MASTER·VOID)", description = "이미 팀에 속한 학생을 다른 팀의 팀원으로 바로 옮긴다(요청 없이 즉시 반영). 팀장은 옮길 수 없다. 학생에게 알림이 발송된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이동 성공"),
            @ApiResponse(responseCode = "400", description = "팀 미소속 학생(배정 요청 사용), 이미 그 팀 소속, 팀장"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님"),
            @ApiResponse(responseCode = "404", description = "학생 또는 프로젝트 없음")
    })
    @PatchMapping("/{userId}/team")
    public ResponseEntity<Void> moveTeam(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학생 userId", example = "12") @PathVariable Long userId,
            @Valid @RequestBody AssignTeamRequest request) {
        adminStudentService.moveTeam(currentUser, userId, request.projectId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "팀에서 제외 (MASTER·VOID)", description = "팀원을 팀에서 제외한다. 팀장은 제외할 수 없다. 학생에게 알림이 발송된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "제외 성공"),
            @ApiResponse(responseCode = "400", description = "팀장"),
            @ApiResponse(responseCode = "403", description = "MASTER·VOID가 아니거나 학생이 이 팀 소속이 아님"),
            @ApiResponse(responseCode = "404", description = "학생 없음")
    })
    @DeleteMapping("/{userId}/team")
    public ResponseEntity<Void> removeFromTeam(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학생 userId", example = "12") @PathVariable Long userId,
            @Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId) {
        adminStudentService.removeFromTeam(currentUser, userId, projectId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀 내 역할 변경 (MASTER)", description = """
            MASTER만 호출할 수 있다.
            - `LEADER`: 이 학생을 팀장으로 지정하고 기존 팀장은 팀원이 된다. `PATCH /api/project/{projectId}/transfer-leader`와 같은 동작이다.
            - `MEMBER`: 이미 팀원이면 아무것도 하지 않는다. 팀장을 팀원으로 바꾸려면 다른 팀원을 LEADER로 지정해야 한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "값 누락, 팀장을 MEMBER로 변경 시도, 이미 팀장"),
            @ApiResponse(responseCode = "403", description = "MASTER가 아니거나 학생이 이 팀 소속이 아님"),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음, 대상이 팀원이 아님"),
            @ApiResponse(responseCode = "409", description = "대상이 다른 프로젝트의 팀장")
    })
    @PatchMapping("/{userId}/team-role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Parameter(description = "학생 userId", example = "12") @PathVariable Long userId,
            @Valid @RequestBody ChangeTeamRoleRequest request) {
        adminStudentService.changeRole(currentUser, userId, request.projectId(), request.role());
        return ResponseEntity.ok().build();
    }
}
