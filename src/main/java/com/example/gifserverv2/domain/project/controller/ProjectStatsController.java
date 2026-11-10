package com.example.gifserverv2.domain.project.controller;

import com.example.gifserverv2.domain.project.dto.response.ProjectStatsResponse;
import com.example.gifserverv2.domain.project.service.ProjectStatsService;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "프로젝트 통계", description = "선생님용 프로젝트별 조회수·좋아요·댓글 수 통계.")
@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectStatsController {

    private final ProjectStatsService statsService;

    @Operation(summary = "프로젝트 통계 조회 (선생님)", description = "선생님(`role=ADMIN`)만 호출할 수 있다. 학년·반으로 거를 수 있고 조회수순 또는 좋아요순으로 정렬한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/stats")
    public ResponseEntity<List<ProjectStatsResponse>> getStats(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "학년. 생략 시 전체", example = "1") @RequestParam(required = false) Integer grade,
            @Parameter(description = "반. 생략 시 전체", example = "2") @RequestParam(required = false) Integer classNo,
            @Parameter(description = "정렬 기준. `VIEWS`(조회수순, 기본) / `LIKES`(좋아요순). 그 외 값은 VIEWS로 처리", example = "VIEWS") @RequestParam(defaultValue = "VIEWS") String sort
    ) {
        // 관리자가 아니면 403. 기존 코드와 같은 예외 방식을 사용한다.
        if (user.role() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "관리자만 조회할 수 있습니다.");
        }
        return ResponseEntity.ok(statsService.getStats(grade, classNo, sort));
    }
}