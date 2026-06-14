package com.example.gifserverv2.domain.project.dto.request;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateProjectRequest {
    private String name;
    private String teamName;
    private String description;
    private List<Long> memberIds;
    private MultipartFile logo;
}