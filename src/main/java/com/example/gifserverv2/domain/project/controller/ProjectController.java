package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.dto.response.ProjectDetailResponse;
import com.example.gifserverv2.domain.project.dto.response.ProjectListResponse;
import com.example.gifserverv2.domain.project.service.ProjectService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<Long> createProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody ProjectCreateRequest request
    ) {
        return ResponseEntity.ok(projectService.createProject(user.userId(), request));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<ProjectListResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/me")
    public ResponseEntity<List<ProjectListResponse>> getMyProjects(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(projectService.getMyProjects(user.userId()));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProject(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProjectListResponse>> filterProjects(
            @RequestParam(required = false) Integer grade
    ) {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody ProjectUpdateNameRequest request
    ) {
        projectService.updateName(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/team-name")
    public ResponseEntity<Void> updateTeamName(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody ProjectUpdateTeamNameRequest request
    ) {
        projectService.updateTeamName(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/description")
    public ResponseEntity<Void> updateDescription(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody ProjectUpdateDescriptionRequest request
    ) {
        projectService.updateDescription(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/members")
    public ResponseEntity<Void> updateMembers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody ProjectUpdateMembersRequest request
    ) {
        projectService.updateMembers(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }
}