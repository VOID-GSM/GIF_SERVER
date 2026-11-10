package com.example.gifserverv2.domain.inquiry.controller;

import com.example.gifserverv2.domain.inquiry.dto.request.AnswerInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.request.UpdateInquiryRequest;
import com.example.gifserverv2.domain.inquiry.dto.response.DetailInquiryResponse;
import com.example.gifserverv2.domain.inquiry.dto.response.ListInquiryResponse;
import com.example.gifserverv2.domain.inquiry.service.AdminInquiryService;
import com.example.gifserverv2.domain.inquiry.service.ClientInquiryService;
import com.example.gifserverv2.domain.inquiry.dto.request.CreateInquiryRequest;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "문의", description = "서비스 이용 문의. 학생·선생님이 문의를 남기면 VOID(운영팀) 계정에 알림이 가고, VOID가 답변하면 작성자에게 알림이 간다.")
@RestController
@RequestMapping("/api/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final ClientInquiryService clientInquiryService;
    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "문의 작성", description = "`multipart/form-data`로 보낸다. 첨부 파일(`file`)은 선택이며 최대 10MB, 서버가 허용하는 확장자만 가능하다. 응답 본문은 문의 ID(숫자)다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "작성 성공. 문의 ID 반환"),
            @ApiResponse(responseCode = "400", description = "제목·내용 누락 또는 길이 초과, 허용되지 않은 파일")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = CreateInquiryRequest.class)))
    @PostMapping
    public ResponseEntity<Long> createInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(hidden = true) @Valid @ModelAttribute CreateInquiryRequest request
    ) {
        return ResponseEntity.ok(clientInquiryService.createInquiry(
                user.userId(),
                request.title(),
                request.content(),
                request.file()
        ));
    }

    @Operation(summary = "내 문의 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/my")
    public ResponseEntity<List<ListInquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiries(user.userId(), user.name()));
    }

    @Operation(summary = "내 문의 상세")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "내 문의가 아님"),
            @ApiResponse(responseCode = "404", description = "문의 없음")
    })
    @GetMapping("/my/{inquiryId}")
    public ResponseEntity<DetailInquiryResponse> getMyInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "문의 ID", example = "3") @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(clientInquiryService.getMyInquiryDetail(user.userId(), user.name(), inquiryId));
    }

    @Operation(summary = "전체 문의 목록 (VOID)", description = "VOID 계정만 조회할 수 있다. `page`(0부터), `size`, `sort`로 페이지를 지정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "VOID 계정이 아님")
    })
    @GetMapping("/admin")
    public ResponseEntity<Page<ListInquiryResponse>> getAllInquiries(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(adminInquiryService.getAllInquiries(user.email(), pageable));
    }

    @Operation(summary = "문의 상세 (VOID)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "VOID 계정이 아님"),
            @ApiResponse(responseCode = "404", description = "문의 없음")
    })
    @GetMapping("/admin/{inquiryId}")
    public ResponseEntity<DetailInquiryResponse> getInquiryDetail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "문의 ID", example = "3") @PathVariable Long inquiryId
    ) {
        return ResponseEntity.ok(adminInquiryService.getInquiryDetail(user.email(), inquiryId));
    }

    @Operation(summary = "문의 답변 (VOID)", description = "답변을 저장하고 상태를 ANSWERED로 바꾼 뒤 작성자에게 알림을 보낸다. 다시 호출하면 답변이 덮어써진다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "답변 성공"),
            @ApiResponse(responseCode = "400", description = "답변 내용 누락"),
            @ApiResponse(responseCode = "403", description = "VOID 계정이 아님"),
            @ApiResponse(responseCode = "404", description = "문의 없음")
    })
    @PatchMapping("/admin/{inquiryId}/answer")
    public ResponseEntity<Void> answerInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "문의 ID", example = "3") @PathVariable Long inquiryId,
            @Valid @RequestBody AnswerInquiryRequest request
    ) {
        adminInquiryService.answerInquiry(user.email(), inquiryId, request.answerContent());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 문의 수정", description = "`multipart/form-data`로 보낸다. 답변 전(PENDING)인 내 문의만 수정할 수 있다. 제목·내용은 보낸 값으로 덮어쓰므로 함께 보내야 한다. `file`을 보내면 기존 첨부 파일을 교체한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 파일"),
            @ApiResponse(responseCode = "403", description = "내 문의가 아님"),
            @ApiResponse(responseCode = "404", description = "문의 없음"),
            @ApiResponse(responseCode = "409", description = "이미 답변된 문의")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = UpdateInquiryRequest.class)))
    @PatchMapping("/my/{inquiryId}")
    public ResponseEntity<Void> updateInquiry(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "문의 ID", example = "3") @PathVariable Long inquiryId,
            @Parameter(hidden = true) @Valid @ModelAttribute UpdateInquiryRequest request
    ) {
        clientInquiryService.updateInquiry(
                user.userId(),
                inquiryId,
                request.title(),
                request.content(),
                request.file()
        );
        return ResponseEntity.noContent().build();
    }
}