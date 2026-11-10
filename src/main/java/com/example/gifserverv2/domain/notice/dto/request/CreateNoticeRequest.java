package com.example.gifserverv2.domain.notice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "공지 작성 요청")
public record CreateNoticeRequest(
        @Schema(description = "제목 (최대 100자)", example = "중간 발표 일정 안내")
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @Schema(description = "내용 (최대 2000자)", example = "11월 20일 오후 2시에 진행합니다.")
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(max = 2000, message = "내용은 2000자를 초과할 수 없습니다.")
        String content,

        @Schema(description = "대상 학년 (선택, 표시용)", example = "[1, 2]")
        List<Integer> targetGrades,
        @Schema(description = "대상 프로젝트 ID (선택, 표시용)", example = "[1, 4]")
        List<Long> targetProjectIds
) {}