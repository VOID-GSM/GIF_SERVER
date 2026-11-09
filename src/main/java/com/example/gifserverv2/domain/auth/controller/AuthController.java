package com.example.gifserverv2.domain.auth.controller;

import com.example.gifserverv2.domain.auth.dto.request.OAuthSignInRequest;
import com.example.gifserverv2.domain.auth.dto.response.OAuthSignInResponse;
import com.example.gifserverv2.domain.auth.service.AuthService;
import com.example.gifserverv2.domain.auth.service.DgOAuthFlowService;
import com.example.gifserverv2.domain.auth.service.GoogleOAuthFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "인증", description = """
        DataGSM(학생·선생님) / Google(외부 사용자) OAuth 로그인.
        로그인 흐름: `/start`로 이동 → 각 OAuth 서버 로그인 → `redirectUri`로 `code`, `state`가 전달됨 →
        프론트가 그 값으로 `/callback`을 호출해 `accessToken`을 받는다.
        이 태그의 API는 모두 로그인 없이 호출한다.""")
@SecurityRequirements
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DgOAuthFlowService dgOAuthFlowService;
    private final GoogleOAuthFlowService googleOAuthFlowService;

    @Operation(summary = "DataGSM 로그인 시작", description = """
            DataGSM 로그인 페이지로 302 리다이렉트한다. 브라우저에서 이 URL로 직접 이동해야 하며 fetch/axios로 호출하지 않는다.
            PKCE·state는 서버가 만들어 5분간 보관하므로 5분 안에 callback까지 완료해야 한다. `returnTo`는 callback 응답에 그대로 돌려준다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "DataGSM 로그인 페이지로 이동"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 redirectUri")
    })
    @GetMapping("/dg/start")
    public ResponseEntity<Void> startDgLogin(
            @Parameter(description = "로그인 후 code/state를 받을 프론트 주소. 서버에 등록된 주소만 허용", example = "https://gif.example.com/auth/callback")
            @RequestParam String redirectUri,
            @Parameter(description = "로그인 완료 후 돌아갈 프론트 내부 경로(선택). `/`로 시작하는 상대 경로만 허용하며, 그 외에는 null로 처리된다", example = "/project/1")
            @RequestParam(required = false) String returnTo) {
        URI location = dgOAuthFlowService.createLoginRedirect(redirectUri, returnTo);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @Operation(summary = "DataGSM 로그인 완료", description = """
            redirectUri로 전달받은 `code`, `state`로 로그인을 완료하고 `accessToken`을 발급한다.
            처음 로그인하면 계정이 생성된다. 선생님은 `role=ADMIN`, 학생은 `role=USER`가 된다.
            `adminRole`(선생님) 또는 `clientRole`(학생)이 null이면 추가 정보 입력(`/api/additional-info/**`)이 필요하다.
            3학년 학생과 승인되지 않은 DataGSM 계정은 로그인할 수 없다. `state`는 한 번만 사용할 수 있다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "code/state 누락, 만료·재사용된 state, DataGSM 사용자 정보 오류"),
            @ApiResponse(responseCode = "403", description = "승인되지 않은 계정, 3학년 학생, 로그인할 수 없는 계정 유형")
    })
    @GetMapping("/dg/callback")
    public OAuthSignInResponse dgCallback(
            @Parameter(description = "DataGSM이 전달한 인가 코드") @RequestParam String code,
            @Parameter(description = "DataGSM이 전달한 state") @RequestParam String state) {
        return dgOAuthFlowService.completeLogin(code, state);
    }

    @Operation(summary = "Google 로그인 시작 (외부 사용자)", description = """
            Google 로그인 페이지로 302 리다이렉트한다. 브라우저에서 이 URL로 직접 이동해야 한다.
            Google 로그인은 교외 방문자(`role=EXTERNAL`)용이다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Google 로그인 페이지로 이동"),
            @ApiResponse(responseCode = "400", description = "허용되지 않은 redirectUri")
    })
    @GetMapping("/google/start")
    public ResponseEntity<Void> startGoogleLogin(
            @Parameter(description = "로그인 후 code/state를 받을 프론트 주소. 서버에 등록된 주소만 허용", example = "https://gif.example.com/auth/google/callback")
            @RequestParam String redirectUri,
            @Parameter(description = "로그인 완료 후 돌아갈 프론트 내부 경로(선택)", example = "/project/1")
            @RequestParam(required = false) String returnTo) {
        URI location = googleOAuthFlowService.createLoginRedirect(redirectUri, returnTo);
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @Operation(summary = "Google 로그인 완료 (외부 사용자)", description = """
            redirectUri로 전달받은 `code`, `state`로 로그인을 완료하고 `accessToken`을 발급한다.
            처음 로그인하면 외부 사용자(`role=EXTERNAL`) 계정이 생성된다.
            같은 이메일의 교내 계정이 이미 있으면 거부되며, DataGSM 로그인을 이용해야 한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "code/state 누락, 만료·재사용된 state, 이메일 정보 없음"),
            @ApiResponse(responseCode = "403", description = "교내 계정의 Google 로그인 시도"),
            @ApiResponse(responseCode = "502", description = "Google 토큰 교환·사용자 정보 조회 실패")
    })
    @GetMapping("/google/callback")
    public OAuthSignInResponse googleCallback(
            @Parameter(description = "Google이 전달한 인가 코드") @RequestParam String code,
            @Parameter(description = "Google이 전달한 state") @RequestParam String state) {
        return googleOAuthFlowService.completeLogin(code, state);
    }

    @Operation(summary = "DataGSM 로그인 (PKCE 직접 처리)", description = """
            프론트가 PKCE를 직접 처리하는 경우에 쓴다. DataGSM에서 받은 인가 코드와 code_verifier로 로그인한다.
            일반적으로는 `/dg/start` → `/dg/callback` 흐름을 사용한다. 계정 생성·로그인 제한 규칙은 `/dg/callback`과 같다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락, 허용되지 않은 redirectUri, DataGSM 사용자 정보 오류"),
            @ApiResponse(responseCode = "403", description = "승인되지 않은 계정, 3학년 학생, 로그인할 수 없는 계정 유형")
    })
    @PostMapping("/signin")
    public OAuthSignInResponse signIn(@Valid @RequestBody OAuthSignInRequest request) {
        return authService.signIn(request);
    }
}
