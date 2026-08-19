package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.RespondAssignmentRequest;
import com.example.gifserverv2.domain.project.service.TeacherAssignmentService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers/assignments")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    @PatchMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> respondAssignment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long assignmentId,
            @Valid @RequestBody RespondAssignmentRequest request) {

        teacherAssignmentService.respondToAssignment(currentUser, assignmentId, request);
        return ResponseEntity.ok().build();
    }
}