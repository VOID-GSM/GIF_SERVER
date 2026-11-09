package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.AdminAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.dto.request.ClientAdditionalInfoRequest;
import com.example.gifserverv2.domain.auth.service.AdditionalInfoService;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Tag(name = "추가 정보", description = """
        첫 로그인 후 역할 정보를 입력한다. 입력이 반영된 새 `accessToken`을 `{ "accessToken": "..." }` 형태로 돌려주므로
        기존 토큰을 이 값으로 교체해야 이후 API에서 역할이 반영된다.""")
@RestController
@RequestMapping("/api/additional-info")
@RequiredArgsConstructor
public class AdditionalInfoController {

    private final AdditionalInfoService additionalInfoService;
    private final AuthService authService;

    @Operation(summary = "선생님 추가 정보 입력", description = """
            선생님(`role=ADMIN`)만 호출할 수 있다. 선생님 역할(`adminRole`), 이름, 학년부장 여부를 저장한다.
            `adminRole`은 보통 `MAJOR_TEACHER`(전공) 또는 `GENERAL_TEACHER`(일반 교과)를 보낸다. 담당 팀(`adminTeam`)은 초기화된다.
            응답: `{ "accessToken": "새 토큰" }`""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공, 새 accessToken 반환"),
            @ApiResponse(responseCode = "400", description = "adminRole 또는 name 누락"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "선생님 계정이 아님")
    })
    @PostMapping("/admin")
    public ResponseEntity<Map<String, String>> updateAdminInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody AdminAdditionalInfoRequest request
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateAdminAdditionalInfo(user.userId(), request);

        String newAccessToken = authService.renewToken(user.userId());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @Operation(summary = "학생 추가 정보 입력", description = """
            학생(`role=USER`)만 호출할 수 있다. 팀 내 역할(`LEADER` 팀장 / `MEMBER` 팀원)을 저장한다.
            응답: `{ "accessToken": "새 토큰" }`""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "저장 성공, 새 accessToken 반환"),
            @ApiResponse(responseCode = "400", description = "clientRole 누락"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "학생 계정이 아님")
    })
    @PostMapping("/client")
    public ResponseEntity<Map<String, String>> updateClientAdditionalInfo(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody ClientAdditionalInfoRequest request
    ) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        additionalInfoService.updateClientAdditionalInfo(user.userId(), request);

        String newAccessToken = authService.renewToken(user.userId());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
