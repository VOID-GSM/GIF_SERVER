package com.example.gifserverv2.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미가입 학생 (명단 기준)")
public record UnregisteredStudentResponse(
        @Schema(description = "학번", example = "1203")
        String studentNumber,
        @Schema(description = "이름", example = "김학생")
        String name,
        @Schema(description = "학년", example = "1")
        Integer grade,
        @Schema(description = "반", example = "2")
        Integer classNo,
        @Schema(description = "번호", example = "3")
        Integer studentNo
) {
}
