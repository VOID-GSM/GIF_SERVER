package com.example.gifserverv2.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 마이페이지 수정. null 인 필드는 바꾸지 않는다.
 *
 * @param major      전공 (빈 문자열이면 삭제)
 * @param techStacks 기술 스택 목록 (빈 목록이면 전체 삭제, 대소문자 무시 중복은 하나만 저장)
 */
@Schema(description = "마이페이지 수정 요청. null인 필드는 바꾸지 않는다")
public record UpdateMypageRequest(
        @Schema(description = "전공 (최대 50자). 빈 문자열이면 삭제", example = "백엔드")
        @Size(max = 50, message = "전공은 50자 이하여야 합니다.")
        String major,

        @Schema(description = "기술 스택 목록 (최대 20개, 각 30자 이하). 빈 배열이면 전체 삭제", example = "[\"Spring\", \"React\"]")
        @Size(max = 20, message = "기술 스택은 최대 20개까지 등록할 수 있습니다.")
        List<@NotBlank(message = "기술 스택은 비어 있을 수 없습니다.")
             @Size(max = 30, message = "기술 스택은 30자 이하여야 합니다.") String> techStacks
) {
}
