package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.dto.response.*;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
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
}