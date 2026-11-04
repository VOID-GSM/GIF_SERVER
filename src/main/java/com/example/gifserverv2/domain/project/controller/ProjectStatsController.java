package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.response.ProjectStatsResponse;
import com.example.gifserverv2.domain.project.service.ProjectStatsService;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectStatsController {

    private final ProjectStatsService statsService;

    @GetMapping("/stats")
    public ResponseEntity<List<ProjectStatsResponse>> getStats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) Integer grade,
            @RequestParam(required = false) Integer classNo,
            @RequestParam(defaultValue = "VIEWS") String sort
    ) {
        // 관리자가 아니면 403. 기존 코드와 같은 예외 방식을 사용한다.
        if (user.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 조회할 수 있습니다.");
        }
        return ResponseEntity.ok(statsService.getStats(grade, classNo, sort));
    }
}