package com.example.gifserverv2.domain.user.controller;

import com.example.gifserverv2.domain.user.dto.response.UserProfileResponse;
import com.example.gifserverv2.domain.user.service.MypageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자 프로필", description = "다른 사용자의 공개 프로필을 조회한다. 로그인한 사용자라면 외부 사용자도 호출할 수 있다.")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final MypageService mypageService;

    @Operation(summary = "공개 프로필 조회", description = """
            프로젝트 팀원 등 다른 사용자의 프로필을 조회한다. 마이페이지 응답에서 `email`, `customProfileImage`를 뺀 형태이며,
            프로필 이미지와 팀 정렬 규칙(팀장인 팀 먼저)은 마이페이지와 같다.
            학생 계정만 조회할 수 있다. 선생님·외부 사용자 계정은 존재 여부를 드러내지 않도록 없는 사용자와 같이 404를 반환한다.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인하지 않음"),
            @ApiResponse(responseCode = "404", description = "없는 사용자이거나 학생 계정이 아님")
    })
    @GetMapping("/{userId:\\d+}/profile")
    public UserProfileResponse getProfile(@Parameter(description = "조회할 사용자 ID", example = "12") @PathVariable Long userId) {
        return mypageService.getProfile(userId);
    }
}
