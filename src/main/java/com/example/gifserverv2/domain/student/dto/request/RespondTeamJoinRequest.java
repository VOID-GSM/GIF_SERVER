package com.example.gifserverv2.domain.student.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RespondTeamJoinRequest(
        @NotNull(message = "수락 여부는 필수입니다.")
        Boolean accept,

        @Size(max = 200, message = "거절 사유는 200자 이내로 입력해주세요.")
        String rejectReason
) {
}
