package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.dto.response.*;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.ProjectLinkService;
import com.example.gifserverv2.domain.project.service.ProjectNoteService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final QueryProjectService projectQueryService;
    private final CommandProjectService projectCommandService;
    private final ProjectNoteService projectNoteService;
    private final ProjectLinkService projectLinkService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> createProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute CreateProjectRequest request
    ) {
        return ResponseEntity.ok(projectCommandService.createProject(user.userId(), request));
    }
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListProjectResponse>> getAllProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectQueryService.getAllProjects(user.userId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ListProjectResponse>> getMyProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectQueryService.getMyProjects(user.userId()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        // 비로그인·외부 사용자는 공개용 응답, 교내 사용자는 기존 응답
        if (user == null || user.role() == Role.EXTERNAL) {
            Long userId = user == null ? null : user.userId();
            return ResponseEntity.ok(projectQueryService.getPublicProject(projectId, userId));
        }
        return ResponseEntity.ok(projectQueryService.getProject(projectId, user.userId()));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ListProjectResponse>> filterProjects(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Integer grade
    ) {
        // 비로그인이면 user 가 null 이다.
        Long userId = user == null ? null : user.userId();
        return ResponseEntity.ok(projectQueryService.getProjectsByGrade(grade, userId));
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = UpdateProjectRequest.class)))
    @PutMapping(value = "/{projectId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @ModelAttribute UpdateProjectRequest request
    ) {
        projectCommandService.updateProject(projectId, user.userId(), request, request.getLogo());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{projectId}/description")
    public ResponseEntity<Void> updateDescription(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectDescriptionRequest request
    ) {
        projectCommandService.updateDescription(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{projectId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadLogo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @RequestPart("file") MultipartFile file
    ) {
        projectCommandService.uploadLogo(projectId, user.userId(), file);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
            @RequestParam String keyword
    ) {
        return ResponseEntity.ok(projectQueryService.searchUsers(keyword));
    }

    //    @GetMapping("/{projectId}/summary")
//    public ResponseEntity<String> summarizeProject(@PathVariable Long projectId) {
//        return ResponseEntity.ok(aiSummaryService.summarizeProject(projectId));
//    }

    @PatchMapping("/{projectId}/transfer-leader")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferLeader(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @RequestBody TransferLeaderRequest request
    ) {
        projectCommandService.transferLeader(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{projectId}/note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> writeOrUpdateNote(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectNoteRequest request
    ) {
        projectNoteService.writeOrUpdateNote(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/note")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetProjectNoteResponse> getMyNote(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(projectNoteService.getMyNote(projectId, user.userId()));
    }

    @GetMapping("/{projectId}/links")
    public ResponseEntity<List<ProjectLinkResponse>> getLinks(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectLinkService.getLinks(projectId));
    }

    @PostMapping("/{projectId}/links")
    public ResponseEntity<Long> createLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectLinkRequest request
    ) {
        return ResponseEntity.ok(projectLinkService.createLink(projectId, user.userId(), request));
    }

    @PatchMapping("/{projectId}/links/{linkId}")
    public ResponseEntity<Void> updateLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @PathVariable Long linkId,
            @Valid @RequestBody UpdateProjectLinkRequest request
    ) {
        projectLinkService.updateLink(projectId, linkId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/links/{linkId}")
    public ResponseEntity<Void> deleteLink(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long projectId,
            @PathVariable Long linkId
    ) {
        projectLinkService.deleteLink(projectId, linkId, user.userId());
        return ResponseEntity.noContent().build();
    }
}