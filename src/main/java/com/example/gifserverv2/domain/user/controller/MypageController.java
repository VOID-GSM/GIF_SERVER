package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.user.dto.request.UpdateMypageRequest;
import com.example.gifserverv2.domain.user.dto.request.UserLinkRequest;
import com.example.gifserverv2.domain.user.dto.response.MypageResponse;
import com.example.gifserverv2.domain.user.dto.response.UserLinkResponse;
import com.example.gifserverv2.domain.user.service.MypageService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
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
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MypageController {

    private final MypageService mypageService;

    @GetMapping("/mypage")
    public MypageResponse getMypage(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.getMypage(requireUserId(user));
    }

    @PatchMapping("/mypage")
    public MypageResponse updateMypage(@AuthenticationPrincipal AuthenticatedUser user,
                                       @Valid @RequestBody UpdateMypageRequest request) {
        return mypageService.updateMypage(requireUserId(user), request);
    }

    @PutMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MypageResponse updateProfileImage(@AuthenticationPrincipal AuthenticatedUser user,
                                             @RequestParam("file") MultipartFile file) {
        return mypageService.updateProfileImage(requireUserId(user), file);
    }

    @DeleteMapping("/profile-image")
    public MypageResponse deleteProfileImage(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.deleteProfileImage(requireUserId(user));
    }

    @GetMapping("/links")
    public List<UserLinkResponse> getLinks(@AuthenticationPrincipal AuthenticatedUser user) {
        return mypageService.getLinks(requireUserId(user));
    }

    @PostMapping("/links")
    public ResponseEntity<UserLinkResponse> addLink(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @Valid @RequestBody UserLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mypageService.addLink(requireUserId(user), request));
    }

    @PatchMapping("/links/{linkId}")
    public UserLinkResponse updateLink(@AuthenticationPrincipal AuthenticatedUser user,
                                       @PathVariable Long linkId,
                                       @Valid @RequestBody UserLinkRequest request) {
        return mypageService.updateLink(requireUserId(user), linkId, request);
    }

    @DeleteMapping("/links/{linkId}")
    public ResponseEntity<Void> deleteLink(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long linkId) {
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
