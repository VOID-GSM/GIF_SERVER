package com.example.gifserverv2.domain.ai.controller;

import com.example.gifserverv2.domain.ai.dto.request.RetrospectiveAiRequest;
import com.example.gifserverv2.domain.ai.dto.response.RetrospectiveAiResponse;
import com.example.gifserverv2.domain.ai.dto.response.RetrospectiveStructureResponse;
import com.example.gifserverv2.domain.ai.service.RetrospectiveAiService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "활동 기록 AI 도움받기", description = "작성 중인 활동 기록을 AI로 다듬거나 구조를 추천받는다. 모든 AI 도움받기 API를 합쳐 사용자당 하루(KST) 20회까지 호출할 수 있고, 실패한 호출은 횟수에 포함되지 않는다. 결과는 저장되지 않으므로 프론트에서 적용 여부를 정한다.")
@RestController
@RequestMapping("/api/ai/retrospective")
@RequiredArgsConstructor
public class RetrospectiveAiController {

    private final RetrospectiveAiService retrospectiveAiService;

    @Operation(summary = "문장 다듬기", description = "맞춤법·띄어쓰기·어색한 문장만 고치고 내용과 마크다운 구조는 유지한 본문을 반환한다. 본문이 20자 이상이어야 한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "카테고리 누락, 본문 20자 미만·5000자 초과, 결과가 잘림(본문을 나눠서 시도)"),
            @ApiResponse(responseCode = "429", description = "오늘 사용 횟수(20회) 초과"),
            @ApiResponse(responseCode = "502", description = "AI 응답 실패. 잠시 후 다시 시도"),
            @ApiResponse(responseCode = "503", description = "서버에 AI 기능이 설정되지 않음")
    })
    @PostMapping("/polish")
    public ResponseEntity<RetrospectiveAiResponse> polish(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.polish(user.userId(), request));
    }

    @Operation(summary = "목차 추천", description = "카테고리에 맞는 목차 4~6개와 그 목차대로 정리한 마크다운 초안을 반환한다. 본문이 비어 있으면 목차와 작성 안내만 담은 템플릿을 반환한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "카테고리 누락, 본문 5000자 초과, 결과가 잘림"),
            @ApiResponse(responseCode = "429", description = "오늘 사용 횟수(20회) 초과"),
            @ApiResponse(responseCode = "502", description = "AI 응답 실패. 잠시 후 다시 시도"),
            @ApiResponse(responseCode = "503", description = "서버에 AI 기능이 설정되지 않음")
    })
    @PostMapping("/structure")
    public ResponseEntity<RetrospectiveStructureResponse> recommendStructure(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.recommendStructure(user.userId(), request));
    }

    @Operation(summary = "포트폴리오용으로 변환", description = "본문을 `## 문제` / `## 해결` / `## 결과` 세 단락의 마크다운으로 바꿔 반환한다. 본문이 20자 이상이어야 한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "카테고리 누락, 본문 20자 미만·5000자 초과, 결과가 잘림(본문을 나눠서 시도)"),
            @ApiResponse(responseCode = "429", description = "오늘 사용 횟수(20회) 초과"),
            @ApiResponse(responseCode = "502", description = "AI 응답 실패. 잠시 후 다시 시도"),
            @ApiResponse(responseCode = "503", description = "서버에 AI 기능이 설정되지 않음")
    })
    @PostMapping("/portfolio")
    public ResponseEntity<RetrospectiveAiResponse> toPortfolio(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody RetrospectiveAiRequest request
    ) {
        return ResponseEntity.ok(retrospectiveAiService.toPortfolio(user.userId(), request));
    }
}
