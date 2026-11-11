package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.dto.response.*;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.ProjectLinkService;
import com.example.gifserverv2.domain.project.service.ProjectNoteService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
import com.example.gifserverv2.global.i18n.Language;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import com.example.gifserverv2.domain.user.entity.Role;

@Tag(name = "프로젝트", description = """
        프로젝트(팀) 생성·조회·수정, 팀원 관리, 외부 링크, 선생님 메모.
        학생은 한 프로젝트에만 소속될 수 있다. 프로젝트를 만든 학생이 팀장(LEADER)이 된다.
        목록·상세 조회는 로그인 없이도 가능하며, 그 외는 별도 표기가 없으면 학생·선생님만 호출할 수 있다.""")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final QueryProjectService projectQueryService;
    private final CommandProjectService projectCommandService;
    private final ProjectNoteService projectNoteService;
    private final ProjectLinkService projectLinkService;
    private final AiSummaryService aiSummaryService;

    @Operation(summary = "프로젝트 생성", description = """
            `multipart/form-data`로 보낸다. 호출한 사용자가 팀장(LEADER)이 되고 `memberIds`의 사용자는 팀원(MEMBER)이 된다.
            `logo`는 png/jpg/jpeg/webp, 최대 10MB.
            생성 후 모든 선생님에게 '프로젝트 생성' 알림이 발송되고, 팀에 들어간 학생의 대기 중인 팀 배정 요청은 취소된다.
            응답 본문은 생성된 프로젝트 ID(숫자)다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공. 프로젝트 ID 반환"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락·길이 초과, 학년 범위(1~2) 오류, 로고 형식·크기 오류"),
            @ApiResponse(responseCode = "404", description = "memberIds에 존재하지 않는 사용자가 있음"),
            @ApiResponse(responseCode = "409", description = "memberIds에 이미 다른 프로젝트에 소속된 사용자가 있음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute CreateProjectRequest request
    ) {
        return ResponseEntity.ok(projectCommandService.createProject(user.userId(), request));
    }
    @Operation(summary = "전체 프로젝트 목록 (선생님)", description = "선생님(`role=ADMIN`)만 호출할 수 있다. 모든 프로젝트를 좋아요·조회수·댓글 수와 함께 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListProjectResponse>> getAllProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectQueryService.getAllProjects(user.userId()));
    }

    @Operation(summary = "내 프로젝트 목록", description = "로그인한 사용자가 팀원으로 속한 프로젝트 목록. 소속이 없으면 빈 배열이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/me")
    public ResponseEntity<List<ListProjectResponse>> getMyProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectQueryService.getMyProjects(user.userId()));
    }

    @Operation(summary = "프로젝트 상세 조회", description = """
            로그인 없이 호출할 수 있다(토큰은 선택). 호출자에 따라 응답 형태가 다르다.
            - 학생·선생님: `DetailProjectResponse` (팀원의 `userId`, `studentNumber` 포함)
            - 비로그인·외부 사용자: `PublicDetailProjectResponse` (팀원 이름·역할만)

            조회만으로는 조회수가 늘지 않는다. 조회수 증가는 `POST /api/project/{projectId}/view`를 따로 호출한다.
            비로그인이면 `isLiked`는 항상 false다.

            `lang=ja`면 `description`을 일본어 AI 번역으로 준다(`descriptionTranslated=true`).
            번역이 아직 없거나 실패했으면 한국어 원문을 주고(`descriptionTranslated=false`), 번역은 뒤에서 만들어 다음 조회부터 반영된다.
            프로젝트명·팀명·팀원 이름은 번역하지 않는다. `lang`이 없거나 `ko`(또는 모르는 값)면 기존과 같다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "응답 언어. ko(기본) / ja", example = "ja") @RequestParam(required = false) String lang
    ) {
        Language language = Language.from(lang);
        // 비로그인·외부 사용자는 공개용 응답, 교내 사용자는 기존 응답
        if (user == null || user.role() == Role.EXTERNAL) {
            Long userId = user == null ? null : user.userId();
            return ResponseEntity.ok(projectQueryService.getPublicProject(projectId, userId, language));
        }
        return ResponseEntity.ok(projectQueryService.getProject(projectId, user.userId(), language));
    }

    @Operation(summary = "학년별 프로젝트 목록", description = "로그인 없이 호출할 수 있다(토큰은 선택). `grade`를 생략하면 전체 프로젝트를 반환한다. 비로그인이면 `isLiked`는 항상 false다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ListProjectResponse>> filterProjects(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "학년 (1 또는 2). 생략 시 전체", example = "1") @RequestParam(required = false) Integer grade
    ) {
        // 비로그인이면 user 가 null 이다.
        Long userId = user == null ? null : user.userId();
        return ResponseEntity.ok(projectQueryService.getProjectsByGrade(grade, userId));
    }

    @Operation(summary = "프로젝트 수정 (팀장)", description = """
            `multipart/form-data`로 보낸다. 팀장만 수정할 수 있다.
            보낸 필드만 바뀌고, 생략한(null) 필드는 기존 값을 유지한다.
            - `addMemberIds`: 추가할 팀원 userId 목록. 추가된 학생에게 알림이 발송된다.
            - `removeMemberIds`: 제외할 팀원 userId 목록. 팀장은 제외할 수 없으며, 제외된 학생에게 알림이 발송된다.
            - `logo`: 새 로고 파일(png/jpg/jpeg/webp, 최대 10MB). 보내면 기존 로고 파일은 삭제된다.

            이름·팀명·설명이 바뀌면 저장된 AI 요약이 초기화된다. 설명이 바뀌면 일본어 번역을 뒤에서 새로 만든다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "학년 범위 오류, 팀장 제외 시도, 로고 형식·크기 오류"),
            @ApiResponse(responseCode = "403", description = "팀장이 아님 / 제외 대상이 팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트"),
            @ApiResponse(responseCode = "409", description = "이미 팀원인 사용자를 추가")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = UpdateProjectRequest.class)))
    @PutMapping(value = "/{projectId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @ModelAttribute UpdateProjectRequest request
    ) {
        projectCommandService.updateProject(projectId, user.userId(), request, request.getLogo());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로젝트 설명 수정 (팀원)", description = "팀원이면 누구나 설명을 수정할 수 있다. `description`이 null이면 아무것도 바뀌지 않는다. 수정하면 저장된 AI 요약이 초기화되고, 일본어 번역을 뒤에서 새로 만든다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "500자 초과"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PatchMapping("/{projectId}/description")
    public ResponseEntity<Void> updateDescription(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectDescriptionRequest request
    ) {
        projectCommandService.updateDescription(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로젝트 로고 업로드 (팀장)", description = "`multipart/form-data`의 `file` 파트로 보낸다. png/jpg/jpeg/webp, 최대 10MB. 기존 로고 파일은 삭제된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "빈 파일, 허용되지 않은 형식, 10MB 초과"),
            @ApiResponse(responseCode = "403", description = "팀장이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping(value = "/{projectId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadLogo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "로고 이미지 파일") @RequestPart("file") MultipartFile file
    ) {
        projectCommandService.uploadLogo(projectId, user.userId(), file);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팀원 후보 검색", description = "이름 또는 학번의 일부로 사용자를 검색한다. `hasTeam`이 true인 사용자는 이미 다른 팀에 속해 있어 팀원으로 추가할 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공. 결과가 없으면 빈 배열")
    })
    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @Parameter(description = "이름 또는 학번 일부", example = "홍길") @RequestParam String keyword
    ) {
        return ResponseEntity.ok(projectQueryService.searchUsers(keyword));
    }

    @Operation(summary = "프로젝트 AI 요약", description = """
            프로젝트명·팀명·설명을 AI가 한 문장으로 요약해 문자열로 반환한다. 만든 요약은 저장해 두고 다음부터 그대로 준다.
            설명을 수정하면 저장된 요약이 초기화되어 다음 호출 때 새로 만든다.
            `lang=ja`면 한국어 요약을 일본어로 번역해 준다. 번역에 실패하면 한국어 요약을 준다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요약 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트"),
            @ApiResponse(responseCode = "502", description = "AI 응답을 받지 못함"),
            @ApiResponse(responseCode = "503", description = "AI 기능이 설정되지 않음")
    })
    @GetMapping("/{projectId}/summary")
    public ResponseEntity<String> summarizeProject(
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "응답 언어. ko(기본) / ja", example = "ja") @RequestParam(required = false) String lang
    ) {
        return ResponseEntity.ok(aiSummaryService.summarizeProject(projectId, Language.from(lang)));
    }

    @Operation(summary = "팀장 양도 (MASTER)", description = """
            아이디어 페스티벌 담당(`adminRole=MASTER`) 선생님만 호출할 수 있다. 같은 프로젝트의 팀원을 새 팀장으로 지정하고 기존 팀장은 팀원이 된다.
            팀원들과 선생님들에게 팀장 변경 알림이 발송된다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "양도 성공"),
            @ApiResponse(responseCode = "400", description = "대상이 이미 이 프로젝트의 팀장"),
            @ApiResponse(responseCode = "401", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "MASTER 선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트, 팀장 없음, 대상이 이 프로젝트 팀원이 아님"),
            @ApiResponse(responseCode = "409", description = "대상이 다른 프로젝트의 팀장")
    })
    @PatchMapping("/{projectId}/transfer-leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferLeader(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody TransferLeaderRequest request
    ) {
        projectCommandService.transferLeader(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로젝트 메모 작성·수정 (선생님)", description = "선생님 본인만 볼 수 있는 프로젝트 메모를 저장한다. 이미 있으면 내용을 덮어쓴다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "내용이 비었거나 1000자 초과"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PutMapping("/{projectId}/note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> writeOrUpdateNote(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectNoteRequest request
    ) {
        projectNoteService.writeOrUpdateNote(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 프로젝트 메모 조회 (선생님)", description = "선생님 본인이 쓴 메모를 반환한다. 작성한 메모가 없으면 `content`는 빈 문자열이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/{projectId}/note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetProjectNoteResponse> getMyNote(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(projectNoteService.getMyNote(projectId, user.userId()));
    }

    @Operation(summary = "프로젝트 링크 목록", description = "로그인한 모든 사용자(외부 사용자 포함)가 호출할 수 있다. 등록 순으로 반환하며, 프로젝트가 없거나 링크가 없으면 빈 배열이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{projectId}/links")
    public ResponseEntity<List<ProjectLinkResponse>> getLinks(@Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId) {
        return ResponseEntity.ok(projectLinkService.getLinks(projectId));
    }

    @Operation(summary = "프로젝트 링크 추가 (팀원)", description = "팀원만 추가할 수 있다. `url`은 `http://` 또는 `https://`로 시작해야 한다. 응답 본문은 생성된 링크 ID(숫자)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추가 성공. 링크 ID 반환"),
            @ApiResponse(responseCode = "400", description = "올바르지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/{projectId}/links")
    public ResponseEntity<Long> createLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectLinkRequest request
    ) {
        return ResponseEntity.ok(projectLinkService.createLink(projectId, user.userId(), request));
    }

    @Operation(summary = "프로젝트 링크 수정 (팀원)", description = "팀원만 수정할 수 있다. 제목과 URL을 모두 보낸 값으로 바꾼다. `url`은 `http://` 또는 `https://`로 시작해야 한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "올바르지 않은 URL"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "링크가 없거나 이 프로젝트의 링크가 아님")
    })
    @PatchMapping("/{projectId}/links/{linkId}")
    public ResponseEntity<Void> updateLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "링크 ID", example = "3") @PathVariable Long linkId,
            @Valid @RequestBody UpdateProjectLinkRequest request
    ) {
        projectLinkService.updateLink(projectId, linkId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "프로젝트 링크 삭제 (팀원)", description = "팀원만 삭제할 수 있다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "링크가 없거나 이 프로젝트의 링크가 아님")
    })
    @DeleteMapping("/{projectId}/links/{linkId}")
    public ResponseEntity<Void> deleteLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable Long projectId,
            @Parameter(description = "링크 ID", example = "3") @PathVariable Long linkId
    ) {
        projectLinkService.deleteLink(projectId, linkId, user.userId());
        return ResponseEntity.noContent().build();
    }
}