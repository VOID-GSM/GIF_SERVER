package com.example.gifserverv2.domain.project.dto.response;

public record GetProjectNoteResponse(
        Long projectId,
        String content
) {
    public static GetProjectNoteResponse of(Long projectId, String content) {
        return new GetProjectNoteResponse(projectId, content);
    }
}
