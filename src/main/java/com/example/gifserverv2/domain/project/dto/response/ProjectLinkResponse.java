package com.example.gifserverv2.domain.project.dto.response;

import com.example.gifserverv2.domain.project.entity.ProjectLink;

public record ProjectLinkResponse(
        Long id,
        String title,
        String url
) {
    public static ProjectLinkResponse from(ProjectLink link) {
        return new ProjectLinkResponse(link.getId(), link.getTitle(), link.getUrl());
    }
}