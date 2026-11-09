package com.example.gifserverv2.domain.student.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "학생 명단 업로드 결과")
public record RosterUploadResponse(
        @Schema(description = "새로 추가된 학생 수", example = "120")
        int created,
        @Schema(description = "이름이 갱신된 학생 수", example = "3")
        int updated,
        @Schema(description = "처리하지 못한 줄 목록")
        List<RowError> errors
) {
    @Schema(description = "처리하지 못한 줄")
    public record RowError(
            @Schema(description = "파일의 줄 번호 (1부터)", example = "15")
            int line,
            @Schema(description = "사유", example = "학번 형식이 올바르지 않습니다: 401")
            String reason
    ) {
    }
}
