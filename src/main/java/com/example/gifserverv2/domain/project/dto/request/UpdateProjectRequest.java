package com.example.gifserverv2.domain.project.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}