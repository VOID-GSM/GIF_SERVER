package com.example.gifserverv2.domain.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "프로젝트 수정 요청 (multipart/form-data). 보낸 필드만 수정되고 생략한 필드는 기존 값을 유지한다")
@Getter
@Setter
@NoArgsConstructor
public class UpdateProjectRequest {
    @Schema(description = "프로젝트 이름", example = "GIF")
    private String name;
    @Schema(description = "팀 이름", example = "VOID")
    private String teamName;
    @Schema(description = "프로젝트 설명", example = "수정된 설명")
    private String description;
    @Schema(description = "추가할 팀원 userId 목록", example = "[21]")
    private List<Long> addMemberIds;
    @Schema(description = "제외할 팀원 userId 목록. 팀장은 제외할 수 없다", example = "[15]")
    private List<Long> removeMemberIds;
    @Schema(description = "새 로고 이미지 (png/jpg/jpeg/webp, 최대 10MB)", type = "string", format = "binary")
    private MultipartFile logo;

    @Schema(description = "학년 (1~2)", example = "2")
    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 2, message = "학년은 2 이하여야 합니다.")
    private Integer grade;
}