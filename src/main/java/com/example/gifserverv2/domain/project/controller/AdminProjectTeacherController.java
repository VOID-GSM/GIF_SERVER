package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.AssignProjectTeacherRequest;
import com.example.gifserverv2.domain.project.dto.response.TeacherListResponse;
import com.example.gifserverv2.domain.project.service.AdminTeacherManagementService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminProjectTeacherController {

    private final AdminTeacherManagementService adminTeacherManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeacherListResponse>> getAllTeachers(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<TeacherListResponse> response = adminTeacherManagementService.getAllTeachers(currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignTeacher(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody AssignProjectTeacherRequest request) {

        adminTeacherManagementService.assignTeacherToProject(currentUser, request);
        return ResponseEntity.ok().build();
    }
}