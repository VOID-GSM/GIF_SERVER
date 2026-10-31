package com.example.gifserverv2.domain.assessment.catchase.dto.response;

import com.example.gifserverv2.domain.assessment.catchase.entity.CatChaseQuestion;

import java.util.List;

public record CatChaseQuestionResponse(
        Long questionId,
        int sequence,
        List<Integer> mousePositions,
        List<Integer> catPositions
) {
    public static CatChaseQuestionResponse from(CatChaseQuestion question) {
        return new CatChaseQuestionResponse(
                question.getId(),
                question.getSequence(),
                List.copyOf(question.getMousePositions()),
                List.copyOf(question.getCatPositions())
        );
    }
}
