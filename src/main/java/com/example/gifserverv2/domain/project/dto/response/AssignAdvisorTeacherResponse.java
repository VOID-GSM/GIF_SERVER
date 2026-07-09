package com.example.gifserverv2.domain.project.dto.response;

public record AssignAdvisorTeacherResponse(
        Long projectId,
        String teamName,
        String projectName,
        Long advisorTeacherId,
        String advisorTeacherName,
        String advisorTeacherEmail
) {
}
