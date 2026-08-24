package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.request.RespondAssignmentRequest;
import com.example.gifserverv2.domain.project.dto.response.MyTeacherAssignmentResponse;
import com.example.gifserverv2.domain.project.service.TeacherAssignmentService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/assignments")
@RequiredArgsConstructor
public class TeacherAssignmentController {

    private final TeacherAssignmentService teacherAssignmentService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MyTeacherAssignmentResponse>> getMyAssignments(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<MyTeacherAssignmentResponse> response = teacherAssignmentService.getMyAssignments(currentUser);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{assignmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> respondAssignment(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long assignmentId,
            @Valid @RequestBody RespondAssignmentRequest request) {

        teacherAssignmentService.respondToAssignment(assignmentId, request, currentUser);
        return ResponseEntity.ok().build();
    }
}