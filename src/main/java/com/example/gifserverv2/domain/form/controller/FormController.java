package com.example.gifserverv2.domain.form.controller;

import com.example.gifserverv2.domain.form.dto.request.*;
import com.example.gifserverv2.domain.form.dto.response.*;
import com.example.gifserverv2.domain.form.service.AdminFormService;
import com.example.gifserverv2.domain.form.service.ClientFormService;
import com.example.gifserverv2.domain.form.service.FormFileService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import com.example.gifserverv2.domain.ai.service.AiSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "양식·제출", description = """
        선생님이 제출 양식(항목: TEXT / FILE / CALENDAR)을 만들어 공지하면, 학생 팀이 양식마다 한 번 제출하고 이후 수정한다.
        `/admin` 경로 및 양식 생성·수정·공지·삭제는 선생님만, 제출 관련 API는 해당 프로젝트 팀원만 호출할 수 있다.
        마감 준수 여부(`deadlineComplied`)는 제출 시각이 마감 이전인지로 계산되며, 선생님이 직접 덮어쓸 수 있다.""")
@RestController
@RequestMapping("/api/form")
@RequiredArgsConstructor
public class FormController {

    private final AdminFormService adminFormService;
    private final ClientFormService clientFormService;
    private final FormFileService formFileService;
    private final AiSummaryService aiSummaryService;

    @Operation(summary = "양식 생성 (MASTER·VOID)", description = """
            `adminRole`이 MASTER 또는 VOID인 선생님만 만들 수 있다. 생성된 양식은 공지 전(초안) 상태이며 응답 본문은 양식 ID(숫자)다.
            FILE 항목의 `allowedExtensions`를 비우면 서버가 허용하는 모든 확장자를 받는다. 마감일은 현재 이후여야 한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공. 양식 ID 반환"),
            @ApiResponse(responseCode = "400", description = "마감일이 과거, 지원하지 않는 확장자"),
            @ApiResponse(responseCode = "401", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody CreateFormRequest request
    ) {
        return ResponseEntity.ok(adminFormService.createForm(user.userId(), request));
    }

    @Operation(summary = "양식 수정 (MASTER·VOID)", description = """
            양식 전체를 요청 내용으로 **덮어쓴다**. 생략한(null) 값은 null로 저장되므로 바꾸지 않을 값도 함께 보내야 한다.
            - `fields`: 기존 항목은 `id`를 넣어 보내고, 새 항목은 `id` 없이 보낸다. 요청에 없는 기존 항목은 삭제된다.
            - 필수 항목을 선택 항목으로 바꿀 수는 없다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "마감일이 과거, 필수→선택 변경, 지원하지 않는 확장자"),
            @ApiResponse(responseCode = "401", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식")
    })
    @PatchMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "양식 ID", example = "3") @RequestParam Long formId,
            @RequestBody UpdateFormRequest request
    ) {
        adminFormService.updateForm(user.userId(), formId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "양식 공지 (선생님)", description = "양식을 공지 상태로 바꾸고 대상 학년(없으면 전체) 학생에게 알림을 보낸다. 공지된 양식만 학생에게 보이고 제출할 수 있다. 다시 호출하면 알림이 또 발송된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공지 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식")
    })
    @PostMapping("/announce")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> announceForm(@Parameter(description = "양식 ID", example = "3") @RequestParam Long formId) {
        adminFormService.announceForm(formId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "양식 삭제 (MASTER·VOID)", description = "양식과 모든 제출 내역, 업로드된 파일을 함께 삭제한다. 되돌릴 수 없다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "선생님이 아니거나 MASTER·VOID가 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식")
    })
    @DeleteMapping("/{formId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "양식 ID", example = "3") @PathVariable("formId") Long formId
    ) {
        adminFormService.deleteForm(user.userId(), formId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "전체 양식 목록 (선생님)", description = "공지 여부와 관계없이 모든 양식을 반환한다. `grade`가 숫자가 아니거나 `null`/`undefined` 문자열이면 전체를 반환한다. 이 응답의 `submitted`는 항상 false다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListFormResponse>> getAllFormsForAdmin(
            @Parameter(description = "대상 학년 (선택)", example = "1") @RequestParam(required = false) String grade
    ) {
        Integer numericGrade = null;
        if (grade != null && !grade.isBlank() && !grade.equals("null")
                && !grade.equals("undefined")) {
            try {
                numericGrade = Integer.parseInt(grade);
            } catch (NumberFormatException e) {
                numericGrade = null;
            }
        }
        return ResponseEntity.ok(adminFormService.getAllFormsForAdmin(numericGrade));
    }

    @Operation(summary = "양식별 제출 목록 (선생님)", description = "해당 양식에 제출한 모든 팀의 답변을 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식")
    })
    @GetMapping("/admin/submit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubmitDetailFormResponse>> getSubmitList(@Parameter(description = "양식 ID", example = "3") @RequestParam Long formId) {
        return ResponseEntity.ok(adminFormService.getSubmitListByForm(formId));
    }

    @Operation(summary = "공지된 양식 목록", description = "공지된 양식을 마감일 오름차순으로 반환한다. `projectId`를 주면 그 팀의 제출 여부(`submitted`)와 마감 준수 여부가 채워진다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<ListFormResponse>> getAnnouncedForms(
            @Parameter(description = "제출 여부를 확인할 프로젝트 ID (선택)", example = "1") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getAnnouncedForms(projectId));
    }

    @Operation(summary = "양식 상세", description = "공지된 양식만 조회할 수 있다(초안은 `/admin/draft/{formId}`). `projectId`를 주면 그 팀의 마감 준수 여부가 채워진다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "공지되지 않은 양식"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식")
    })
    @GetMapping("/{formId}")
    public ResponseEntity<DetailFormResponse> getForm(
            @Parameter(description = "양식 ID", example = "3") @PathVariable Long formId,
            @Parameter(description = "마감 준수 여부를 확인할 프로젝트 ID (선택)", example = "1") @RequestParam(required = false) Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getForm(formId, projectId));
    }

    @Operation(summary = "양식 제출 (팀원)", description = """
            해당 프로젝트 팀원만 제출할 수 있고, 한 양식에 팀당 한 번만 제출할 수 있다(이후는 `PATCH /api/form/submit`로 수정).
            필수 항목은 항목 유형에 맞는 답(TEXT: `textAnswer`, FILE: `filePath`, CALENDAR: `dateAnswer`)이 있어야 한다. 텍스트 답변은 최대 10000자.
            마감 이후에도 제출할 수 있으며, 이 경우 `deadlineComplied`가 false가 된다. 응답 본문은 제출 ID(숫자)다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "제출 성공. 제출 ID 반환"),
            @ApiResponse(responseCode = "400", description = "공지되지 않은 양식, 필수 답변 누락, 10000자 초과, 날짜 형식(yyyy-MM-dd)·기간 오류"),
            @ApiResponse(responseCode = "403", description = "해당 프로젝트 팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 양식 또는 항목"),
            @ApiResponse(responseCode = "409", description = "이미 제출한 양식")
    })
    @PostMapping("/submit")
    public ResponseEntity<Long> submitForm(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody SubmitFormRequest request
    ) {
        return ResponseEntity.ok(clientFormService.submitForm(user.userId(), request));
    }

    @Operation(summary = "우리 팀 제출 내역", description = "프로젝트의 해당 양식 제출 내역과 답변을 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "제출 내역 없음")
    })
    @GetMapping("/my-submit")
    public ResponseEntity<SubmitDetailFormResponse> getMySubmit(
            @Parameter(description = "양식 ID", example = "3") @RequestParam Long formId,
            @Parameter(description = "프로젝트 ID", example = "1") @RequestParam Long projectId
    ) {
        return ResponseEntity.ok(clientFormService.getMySubmit(formId, projectId));
    }

    @Operation(summary = "제출 파일 업로드 (팀원)", description = """
            `multipart/form-data`로 보낸다. 이미 만들어진 제출(`submitId`)의 FILE 항목에 파일을 올린다. 최대 10MB, 항목에 허용된 확장자만 가능하다.
            같은 항목에 이미 파일이 있으면 기존 파일을 지우고 교체한다. 반환된 `filePath`를 이후 제출 수정 시 그대로 보내면 된다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "확장자 없음·허용되지 않은 형식, FILE 항목이 아님, 10MB 초과"),
            @ApiResponse(responseCode = "403", description = "해당 프로젝트 팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "제출 내역 또는 항목을 찾을 수 없음")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "제출 ID", example = "8") @RequestParam Long submitId,
            @Parameter(description = "FILE 항목 ID", example = "21") @RequestParam Long fieldId,
            @Parameter(description = "업로드할 파일") @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(formFileService.uploadFile(user.userId(), submitId, fieldId, file));
    }

    @Operation(summary = "제출 파일 삭제 (팀원)", description = "해당 항목의 답변과 저장된 파일을 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "해당 프로젝트 팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "제출 내역 또는 파일을 찾을 수 없음")
    })
    @DeleteMapping("/upload")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "제출 ID", example = "8") @RequestParam Long submitId,
            @Parameter(description = "FILE 항목 ID", example = "21") @RequestParam Long fieldId
    ) {
        formFileService.deleteFile(user.userId(), submitId, fieldId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "제출 수정 (팀원)", description = """
            제출의 답변 전체를 요청 내용으로 **교체한다**. 요청에 없는 항목의 기존 답변은 삭제되므로 파일 답변도 `filePath`를 다시 보내야 한다.
            필수 항목 규칙과 10000자 제한은 제출과 같다. 마지막 제출자가 호출한 사용자로 바뀌고 AI 요약이 초기화된다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "answers 누락, 필수 답변 누락, 이 양식에 없는 항목, 10000자 초과, 날짜 기간 오류"),
            @ApiResponse(responseCode = "403", description = "해당 프로젝트 팀원이 아님"),
            @ApiResponse(responseCode = "404", description = "제출 내역 또는 항목을 찾을 수 없음")
    })
    @PatchMapping("/submit")
    public ResponseEntity<Void> updateSubmit(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody UpdateSubmitRequest request
    ) {
        clientFormService.updateSubmit(user.userId(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "초안(미공지) 양식 목록 (선생님)", description = "공지되지 않은 양식을 최신순으로 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님")
    })
    @GetMapping("/admin/draft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ListFormResponse>> getDraftForms() {
        return ResponseEntity.ok(adminFormService.getDraftForms());
    }

    @Operation(summary = "초안(미공지) 양식 상세 (선생님)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "양식이 없거나 이미 공지됨")
    })
    @GetMapping("/admin/draft/{formId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DetailFormResponse> getDraftForm(@Parameter(description = "양식 ID", example = "3") @PathVariable Long formId) {
        return ResponseEntity.ok(adminFormService.getDraftForm(formId));
    }

//    @GetMapping("/submit/{submitId}/summary")
//    public ResponseEntity<String> summarizeFormSubmit(
//            @AuthenticationPrincipal AuthenticatedUser user,
//            @PathVariable Long submitId
//    ) {
//        return ResponseEntity.ok(aiSummaryService.summarizeFormSubmit(submitId));
//    }

    @Operation(summary = "마감 준수 여부 수정 (선생님)", description = "제출의 마감 준수 여부를 직접 지정한다. `deadlineComplied`를 null로 보내면 지정을 해제하고 제출 시각 기준 자동 계산으로 돌아간다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "선생님이 아님"),
            @ApiResponse(responseCode = "404", description = "제출 내역 없음")
    })
    @PatchMapping("/admin/submit/{submitId}/deadline-compliance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSubmitDeadlineCompliance(
            @Parameter(description = "제출 ID", example = "8") @PathVariable Long submitId,
            @RequestBody DeadlineComplianceRequest request
    ) {
        adminFormService.updateSubmitDeadlineCompliance(submitId, request.deadlineComplied());
        return ResponseEntity.noContent().build();
    }
}

