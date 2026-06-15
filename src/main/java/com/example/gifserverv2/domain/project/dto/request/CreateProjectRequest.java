package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreateProjectRequest(
        @NotBlank(message = "프로젝트 이름은 필수입니다.")
        @Size(max = 20, message = "프로젝트 이름은 20자 이하여야 합니다.")
        String name,

        @NotBlank(message = "팀 이름은 필수입니다.")
        @Size(max = 20, message = "팀 이름은 20자 이하여야 합니다.")
        String teamName,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다.")
        String description,

        @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
        @Max(value = 2, message = "학년은 2 이하여야 합니다.")
        Integer grade,
        List<Long> memberIds,
        MultipartFile logo
) {}