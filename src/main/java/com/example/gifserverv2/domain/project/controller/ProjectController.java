package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final QueryProjectService projectQueryService;
    private final CommandProjectService projectCommandService;
    private final AiSummaryService aiSummaryService;
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
    public ResponseEntity<List<ListProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectQueryService.getAllProjects());
    }

    @GetMapping("/me")
    public ResponseEntity<List<ListProjectResponse>> getMyProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectQueryService.getMyProjects(user.userId()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<DetailProjectResponse> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectQueryService.getProject(projectId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ListProjectResponse>> filterProjects(
            @RequestParam(required = false) Integer grade
    ) {
        return ResponseEntity.ok(projectQueryService.getProjectsByGrade(grade));
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

    @GetMapping("/{projectId}/summary")
    public ResponseEntity<String> summarizeProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(aiSummaryService.summarizeProject(projectId));
    }

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