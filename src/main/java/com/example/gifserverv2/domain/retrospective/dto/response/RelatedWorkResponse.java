package com.example.gifserverv2.domain.retrospective.dto.response;

import com.example.gifserverv2.domain.retrospective.entity.RelatedWorkType;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveRelatedWork;

public record RelatedWorkResponse(
        RelatedWorkType type,
        Integer number,
        String title
) {
    public static RelatedWorkResponse from(RetrospectiveRelatedWork work) {
        return new RelatedWorkResponse(work.getType(), work.getNumber(), work.getTitle());
    }
}
