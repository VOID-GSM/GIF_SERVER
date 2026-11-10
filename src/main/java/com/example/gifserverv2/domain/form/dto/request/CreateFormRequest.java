package com.example.gifserverv2.domain.form.dto.request;

import com.example.gifserverv2.domain.form.entity.FormField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "양식 생성 요청")
public record CreateFormRequest(
        @Schema(description = "양식 제목", example = "1차 중간 보고서")
        String title,
        @Schema(description = "마감 일시. 현재 이후여야 한다", example = "2026-11-30T23:59:59")
        LocalDateTime deadline,
        @Schema(description = "항목 목록")
        List<FieldRequest> fields
) {
    @Schema(description = "양식 항목")
    public record FieldRequest(
            @Schema(description = "항목 제목", example = "프로젝트 진행 상황")
            String title,
            @Schema(description = "항목 설명", example = "이번 주까지 진행한 내용을 적어 주세요.")
            String description,
            @Schema(description = "항목 유형. TEXT(텍스트) / FILE(파일) / CALENDAR(일정)", example = "TEXT")
            FormField.FieldType type,
            @Schema(description = "표시 순서 (작을수록 위)", example = "0")
            int orderIndex,
            @Schema(description = "FILE 항목의 허용 확장자 목록. 점(.) 유무·대소문자 무관. 비우면 서버가 허용하는 전체 확장자", example = "[\"pdf\", \"pptx\"]")
            List<String> allowedExtensions,
            @Schema(description = "필수 항목 여부", example = "true")
            boolean required
    ) {}
}