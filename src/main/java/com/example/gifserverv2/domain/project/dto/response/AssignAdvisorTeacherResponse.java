package com.example.gifserverv2.domain.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 교사 지정 결과")
public record AssignAdvisorTeacherResponse(
        @Schema(description = "프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "팀 이름", example = "VOID")
        String teamName,
        @Schema(description = "프로젝트 이름", example = "GIF")
        String projectName,
        @Schema(description = "지도 교사 userId", example = "7")
        Long advisorTeacherId,
        @Schema(description = "지도 교사 이름", example = "김선생")
        String advisorTeacherName,
        @Schema(description = "지도 교사 이메일", example = "teacher@gsm.hs.kr")
        String advisorTeacherEmail
) {
}
