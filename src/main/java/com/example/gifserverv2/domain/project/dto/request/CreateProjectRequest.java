package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "프로젝트 생성 요청 (multipart/form-data)")
public record CreateProjectRequest(
        @Schema(description = "프로젝트 이름 (최대 20자)", example = "GIF")
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        @Size(max = 20, message = "프로젝트 이름은 20자 이하여야 합니다.")
        String name,

        @Schema(description = "팀 이름 (최대 20자)", example = "VOID")
        @NotBlank(message = "팀 이름은 필수입니다.")
        @Size(max = 20, message = "팀 이름은 20자 이하여야 합니다.")
        String teamName,

        @Schema(description = "프로젝트 설명 (최대 500자, 선택)", example = "아이디어 페스티벌 운영 플랫폼")
        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        @Schema(description = "학년 (1~2, 선택)", example = "2")
        @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
        @Max(value = 2, message = "학년은 2 이하여야 합니다.")
        Integer grade,
        @Schema(description = "팀원으로 추가할 사용자 userId 목록 (선택). 본인은 자동으로 팀장이 되므로 넣지 않아도 된다", example = "[12, 15]")
        List<Long> memberIds,
        @Schema(description = "로고 이미지 (png/jpg/jpeg/webp, 최대 10MB, 선택)", type = "string", format = "binary")
        MultipartFile logo
) {}