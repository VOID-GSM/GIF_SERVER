package com.example.gifserverv2.domain.score.dto.response;

import java.util.List;

/**
 * 프로젝트 하나에 대한 로그인한 선생님의 항목별 채점 현황. 삭제되지 않은 영역/항목만 담는다.
 * 채점하지 않은 항목은 score 가 null 이다.
 */
public record ProjectItemScoreResponse(
        Long projectId,
        int totalScore,
        int totalMaxScore,
        List<Area> areas
) {
    public record Area(
            Long id,
            String name,
            int totalScore,
            int totalMaxScore,
            List<Item> items
    ) {
    }

    public record Item(
            Long id,
            String name,
            String criteria,
            int maxScore,
            List<Integer> options,
            Integer score
    ) {
    }
}
