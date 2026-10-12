package com.example.gifserverv2.domain.retrospective.dto.response;

public record AuthorAffiliation(
        Integer grade,
        Integer classNo,
        Integer teamNo,
        String part,
        Long projectId,
        String teamName
) {
    public static AuthorAffiliation empty() {
        return new AuthorAffiliation(null, null, null, null, null, null);
    }
}
