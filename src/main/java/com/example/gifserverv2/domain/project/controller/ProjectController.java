package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.dto.response.*;
import com.example.gifserverv2.domain.project.service.CommandProjectService;
import com.example.gifserverv2.domain.project.service.QueryProjectService;
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

    private final QueryProjectService projectQueryService;
    private final CommandProjectService projectCommandService;

    @PostMapping
    public ResponseEntity<Long> createProject(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CreateProjectRequest request
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

    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody UpdateNameProjectRequest request
    ) {
        projectCommandService.updateName(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/team-name")
    public ResponseEntity<Void> updateTeamName(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody UpdateTeamNameProjectRequest request
    ) {
        projectCommandService.updateTeamName(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/description")
    public ResponseEntity<Void> updateDescription(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody UpdateDescriptionProjectRequest request
    ) {
        projectCommandService.updateDescription(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/members")
    public ResponseEntity<Void> updateMembers(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam Long projectId,
            @RequestBody UpdateMembersProjectRequest request
    ) {
        projectCommandService.updateMembers(projectId, user.userId(), request);
        return ResponseEntity.noContent().build();
    }
}