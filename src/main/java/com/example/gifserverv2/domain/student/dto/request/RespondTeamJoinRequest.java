package com.example.gifserverv2.domain.student.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "팀원 추가 요청 응답")
public record RespondTeamJoinRequest(
        @Schema(description = "true: 수락, false: 거절", example = "true")
        @NotNull(message = "수락 여부는 필수입니다.")
        Boolean accept,

        @Schema(description = "거절 사유 (선택, 최대 200자)")
        @Size(max = 200, message = "거절 사유는 200자 이내로 입력해주세요.")
        String rejectReason
) {
}
