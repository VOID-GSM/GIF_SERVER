package com.example.gifserverv2.domain.auth.dto.request;

import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "내 정보 수정 요청. 생략한 값은 유지된다")
public record UpdateCurrentUserRequest(
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "학번 (숫자만, 학생 전용)", example = "2101")
        @Pattern(regexp = "\\d*", message = "studentNumber must be numeric")
        String studentNumber,
        @Schema(description = "선생님 역할 (선생님 전용)", example = "MAJOR_TEACHER")
        AdminRole adminRole,
        @Schema(description = "담당 팀 이름 (선생님 전용)", example = "VOID")
        String adminTeam,
        @Schema(description = "팀 내 역할 LEADER / MEMBER (학생 전용)", example = "MEMBER")
        ClientRole clientRole
) {
}
