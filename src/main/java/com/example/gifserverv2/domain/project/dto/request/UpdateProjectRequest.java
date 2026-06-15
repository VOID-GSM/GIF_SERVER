package com.example.gifserverv2.domain.project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProjectRequest {
    private String name;
    private String teamName;
    private String description;
    private List<Long> addMemberIds;
    private List<Long> removeMemberIds;
    private MultipartFile logo;

    @Min(value = 1, message = "학년은 1 이상이어야 합니다.")
    @Max(value = 2, message = "학년은 2 이하여야 합니다.")
    private Integer grade;
}