package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.user.dto.request.UpdateMypageRequest;
import com.example.gifserverv2.domain.user.dto.request.UserLinkRequest;
import com.example.gifserverv2.domain.user.dto.response.MypageResponse;
import com.example.gifserverv2.domain.user.dto.response.UserLinkResponse;
import com.example.gifserverv2.domain.user.service.MypageService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 마이페이지 (#142). 활동 기록·북마크 목록은 기존 GET /api/retrospective?mine=true / ?bookmarkedOnly=true 를 사용한다.
 */
@Tag(name = "마이페이지", description = """
        프로필 이미지·전공·기술 스택·개인 링크 관리. 활동 기록·북마크 목록은 `GET /api/retrospective?mine=true` / `?bookmarkedOnly=true`를 사용한다.
        외부 사용자(`role=EXTERNAL`)도 호출할 수 있다. 외부 사용자는 `studentNumber`·`grade`·`classNo`가 null, `teams`가 빈 배열이다.""")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @Operation(summary = "마이페이지 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @GetMapping("/mypage")
    public MypageResponse getMypage(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.getMypage(requireUserId(user));
    }

    @Operation(summary = "마이페이지 수정", description = "보낸 값만 바뀐다. `major`를 빈 문자열로 보내면 삭제되고, `techStacks`를 빈 배열로 보내면 전체 삭제된다. 기술 스택은 대소문자를 무시하고 중복을 하나로 합친다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "전공 50자 초과, 기술 스택 20개·30자 초과 또는 빈 값"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @PatchMapping("/mypage")
    public MypageResponse updateMypage(@AuthenticationPrincipal AuthenticatedUser user,
                                       @Valid @RequestBody UpdateMypageRequest request) {
        return mypageService.updateMypage(requireUserId(user), request);
    }

    @Operation(summary = "프로필 이미지 업로드", description = "`multipart/form-data`의 `file` 파트로 보낸다. png/jpg/jpeg/webp, 최대 5MB. 기존에 올린 이미지는 교체된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "빈 파일, 허용되지 않은 형식, 5MB 초과"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MypageResponse updateProfileImage(@AuthenticationPrincipal AuthenticatedUser user,
                                             @Parameter(description = "프로필 이미지 파일") @RequestParam("file") MultipartFile file) {
        return mypageService.updateProfileImage(requireUserId(user), file);
    }

    @Operation(summary = "프로필 이미지 삭제", description = "직접 올린 이미지를 삭제한다. 이후 프로필 이미지는 GitHub 아바타(없으면 null)로 표시된다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @DeleteMapping("/profile-image")
    public MypageResponse deleteProfileImage(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.deleteProfileImage(requireUserId(user));
    }

    @Operation(summary = "내 개인 링크 목록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음")
    })
    @GetMapping("/links")
    public List<UserLinkResponse> getLinks(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.getLinks(requireUserId(user));
    }

    @Operation(summary = "개인 링크 추가", description = "http/https 주소만 등록할 수 있으며 최대 5개까지 등록할 수 있다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "추가 성공"),
            @ApiResponse(responseCode = "400", description = "주소 누락·형식 오류, 길이 초과"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "5개 초과")
    })
    @PostMapping("/links")
    public ResponseEntity<UserLinkResponse> addLink(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @Valid @RequestBody UserLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mypageService.addLink(requireUserId(user), request));
    }

    @Operation(summary = "개인 링크 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "주소 누락·형식 오류, 길이 초과"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "링크가 없거나 내 링크가 아님")
    })
    @PatchMapping("/links/{linkId}")
    public UserLinkResponse updateLink(@AuthenticationPrincipal AuthenticatedUser user,
                                       @Parameter(description = "링크 ID", example = "2") @PathVariable Long linkId,
                                       @Valid @RequestBody UserLinkRequest request) {
        return mypageService.updateLink(requireUserId(user), linkId, request);
    }

    @Operation(summary = "개인 링크 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않았거나 사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "404", description = "링크가 없거나 내 링크가 아님")
    })
    @DeleteMapping("/links/{linkId}")
    public ResponseEntity<Void> deleteLink(@AuthenticationPrincipal AuthenticatedUser user,
                                           @Parameter(description = "링크 ID", example = "2") @PathVariable Long linkId) {
        mypageService.deleteLink(requireUserId(user), linkId);
        return ResponseEntity.noContent().build();
    }

    private static Long requireUserId(AuthenticatedUser user) {
        if (user == null || user.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
        return user.userId();
    }
}
