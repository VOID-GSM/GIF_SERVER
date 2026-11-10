package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "선생님 추가 정보 입력 요청")
public record AdminAdditionalInfoRequest(
        @Schema(description = "선생님 역할. MAJOR_TEACHER(전공 교과) / GENERAL_TEACHER(일반 교과)", example = "MAJOR_TEACHER")
        @NotNull(message = "선생님 역할은 필수입니다.") AdminRole adminRole,
        @Schema(description = "선생님 이름", example = "김선생")
        @NotBlank(message = "이름은 필수입니다.") String name,
        @Schema(description = "학년부장 여부. 생략하면 false", example = "false")
        boolean gradeHead
) {
}