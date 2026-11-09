package com.example.gifserverv2.domain.score.controller;

import com.example.gifserverv2.domain.score.dto.request.*;
import com.example.gifserverv2.domain.score.dto.response.GetProjectFieldAverageResponse;
import com.example.gifserverv2.domain.score.entity.Score;
import com.example.gifserverv2.domain.score.service.*;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.gifserverv2.domain.score.dto.response.GetScoreRankResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.gifserverv2.domain.score.dto.response.GetDetailScoreResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "점수 (영역별 채점)", description = """
        선생님이 프로젝트를 전공(MAJOR)·보고서(REPORT)·사회(SOCIAL) 영역으로 나누어 채점한다.
        - 전공: `adminRole` = MAJOR_TEACHER 또는 MASTER
        - 보고서: 학년부장(`gradeHead=true`)
        - 사회: `adminRole` = GENERAL_TEACHER

        점수는 (프로젝트, 선생님)마다 한 건이며 영역별 저장 API는 해당 영역 항목만 바꾼다. 점수 값의 범위는 서버에서 검사하지 않는다.
        평가 기간 밖에는 저장할 수 없다.""")
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoreController {

    private final MajorScoreService majorScoreService;
    private final ReportScoreService reportScoreService;
    private final SocialScoreService socialScoreService;
    private final ScoreNoticeService scoreNoticeService;
    private final ScoreQueryService scoreQueryService;
    private final EvaluationPeriodService evaluationPeriodService;

    private String evaluatorId(AuthenticatedUser user) {
        return user.userId().toString();
    }

    @Operation(summary = "전공 점수 저장 (MAJOR_TEACHER·MASTER)", description = """
            MAJOR_TEACHER·MASTER만 채점할 수 있다. VOID 계정은 채점할 수 없다.
            로그인한 선생님 기준으로 해당 프로젝트 점수가 없으면 만들고, 있으면 전공 항목만 덮어쓴다. `technicalCompleteness`, `socialValueMajor`, `aiUtilityMajorScore`, `presentationMajor`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, projectId 또는 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MAJOR_TEACHER·MASTER가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/major")
    public ResponseEntity<Void> createMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @RequestBody CreateMajorScoreRequest request) {
        majorScoreService.createMajor(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "전공 점수 수정 (MAJOR_TEACHER·MASTER)", description = """
            이미 저장한 내 전공 점수를 수정한다. 점수가 없으면 404이므로 처음에는 POST로 저장한다. `technicalCompleteness`, `socialValueMajor`, `aiUtilityMajorScore`, `presentationMajor`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MAJOR_TEACHER·MASTER가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트 또는 저장된 내 점수 없음")
    })
    @PatchMapping("/major/{projectId}")
    public ResponseEntity<Void> updateMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId,
                                            @RequestBody PatchMajorScoreRequest request) {
        majorScoreService.updateMajor(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 전공 점수 조회 (선생님)", description = "로그인한 선생님이 해당 프로젝트에 매긴 점수 한 건을 반환한다(다른 영역 점수도 함께 담긴다). 아직 채점하지 않았으면 모든 값이 null(`rank`는 0)인 객체를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/major")
    public ResponseEntity<GetDetailScoreResponse> getMajor(@AuthenticationPrincipal AuthenticatedUser user,
                                                           @Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId) {
        Score score = majorScoreService.getMajor(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @Operation(summary = "보고서 점수 저장 (학년부장)", description = """
            학년부장만 채점할 수 있다. VOID 계정은 채점할 수 없다.
            로그인한 선생님 기준으로 해당 프로젝트 점수가 없으면 만들고, 있으면 보고서 항목만 덮어쓴다. `reportWriting`, `reportContent`, `aiUsagePlan`, `creativity`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, projectId 또는 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 학년부장가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/report")
    public ResponseEntity<Void> createReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateReportScoreRequest request) {
        reportScoreService.createReport(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "보고서 점수 수정 (학년부장)", description = """
            이미 저장한 내 보고서 점수를 수정한다. 점수가 없으면 404이므로 처음에는 POST로 저장한다. `reportWriting`, `reportContent`, `aiUsagePlan`, `creativity`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 학년부장가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트 또는 저장된 내 점수 없음")
    })
    @PatchMapping("/report/{projectId}")
    public ResponseEntity<Void> updateReport(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId,
                                             @RequestBody PatchReportScoreRequest request) {
        reportScoreService.updateReport(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 보고서 점수 조회 (선생님)", description = "로그인한 선생님이 해당 프로젝트에 매긴 점수 한 건을 반환한다(다른 영역 점수도 함께 담긴다). 아직 채점하지 않았으면 모든 값이 null(`rank`는 0)인 객체를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/report")
    public ResponseEntity<GetDetailScoreResponse> getReport(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId) {
        Score score = reportScoreService.getReport(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @Operation(summary = "사회 점수 저장 (GENERAL_TEACHER)", description = """
            GENERAL_TEACHER만 채점할 수 있다. VOID 계정은 채점할 수 없다.
            로그인한 선생님 기준으로 해당 프로젝트 점수가 없으면 만들고, 있으면 사회 항목만 덮어쓴다. `userExperience`, `socialValueCommunity`, `aiUtilizationCommunity`, `presentationCommunity`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, projectId 또는 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 GENERAL_TEACHER가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @PostMapping("/social")
    public ResponseEntity<Void> createSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestBody CreateSocialScoreRequest request) {
        socialScoreService.createSocial(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "사회 점수 수정 (GENERAL_TEACHER)", description = """
            이미 저장한 내 사회 점수를 수정한다. 점수가 없으면 404이므로 처음에는 POST로 저장한다. `userExperience`, `socialValueCommunity`, `aiUtilizationCommunity`, `presentationCommunity`는 모두 필수다.
            평가 기간(기본: 12월 28일 00:00 ~ 12월 29일 23:59:59, 시작일은 MASTER가 변경)에만 저장할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "평가 기간이 아님, 점수 누락"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 GENERAL_TEACHER가 아님, VOID 계정"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트 또는 저장된 내 점수 없음")
    })
    @PatchMapping("/social/{projectId}")
    public ResponseEntity<Void> updateSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId,
                                             @RequestBody PatchSocialScoreRequest request) {
        socialScoreService.updateSocial(projectId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 사회 점수 조회 (선생님)", description = "로그인한 선생님이 해당 프로젝트에 매긴 점수 한 건을 반환한다(다른 영역 점수도 함께 담긴다). 아직 채점하지 않았으면 모든 값이 null(`rank`는 0)인 객체를 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/social")
    public ResponseEntity<GetDetailScoreResponse> getSocial(@AuthenticationPrincipal AuthenticatedUser user,
                                                            @Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId) {
        Score score = socialScoreService.getSocial(projectId, user);
        return ResponseEntity.ok(new GetDetailScoreResponse(score));
    }

    @Operation(summary = "전체 프로젝트 영역별 평균 (선생님)", description = "모든 프로젝트에 대해 채점한 선생님들의 영역별 합계 평균을 반올림한 정수로 반환한다. 채점이 없으면 0이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/projects/averages")
    public ResponseEntity<List<GetProjectFieldAverageResponse>> getAllProjectFieldAverages(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return ResponseEntity.ok(scoreQueryService.getAllProjectFieldAverages(user));
    }

    @Operation(summary = "프로젝트 영역별 평균 (선생님)", description = "한 프로젝트의 영역별 합계 평균을 반올림한 정수로 반환한다. `grandTotalAverage`는 세 영역 평균의 평균이다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 프로젝트")
    })
    @GetMapping("/projects/{projectId}/averages")
    public ResponseEntity<GetProjectFieldAverageResponse> getProjectFieldAverages(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "프로젝트 ID", example = "1") @PathVariable("projectId") Long projectId) {

        GetProjectFieldAverageResponse response = scoreQueryService.getProjectFieldAverages(projectId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "프로젝트 순위", description = """
            학생·선생님 모두 호출할 수 있다. 프로젝트별로 선생님들이 준 총점(`subTotalScore`)의 평균을 반올림해 높은 순으로 정렬한다. 동점은 같은 순위다.
            `rank`를 주면 그 순위의 팀만 반환한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공. 프로젝트가 없으면 빈 배열")
    })
    @GetMapping("/rank")
    public ResponseEntity<List<GetScoreRankResponse>> getRank(
            @Parameter(description = "학년 (선택). 생략 시 전체", example = "2") @RequestParam(required = false) Integer grade,
            @Parameter(description = "조회할 순위 (선택)", example = "1") @RequestParam(required = false) Integer rank) {

        return ResponseEntity.ok(scoreNoticeService.getRankByGradeAndRank(grade, rank));
    }

    @Operation(summary = "평가 시작일 설정 (MASTER)", description = "영역별 평가 시작일을 바꾼다. 마감은 올해 12월 29일 23:59:59로 고정이며 시작일은 그보다 이전이어야 한다. MASTER가 아니면 **400**을 응답한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공"),
            @ApiResponse(responseCode = "400", description = "MASTER가 아님, 값 누락, 시작일이 마감 이후"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @PostMapping("/period")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePeriod(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody UpdateEvaluationStartDateRequest request) {

        evaluationPeriodService.updateStartDate(
                currentUser,
                request.getCategory(),
                request.getStartDate()
        );
        return ResponseEntity.ok().build();
    }
}
