package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.response.ProjectStatsResponse;
import com.example.gifserverv2.domain.project.repository.ProjectStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectStatsService {

    private final ProjectStatsRepository statsRepository;


    @Transactional(readOnly = true)
    public List<ProjectStatsResponse> getStats(Integer grade, Integer classNo, String sort) {
        // 허용된 값(LIKES)만 인정하고, 그 외는 모두 조회수순으로 처리한다.
        String safeSort = "LIKES".equalsIgnoreCase(sort) ? "LIKES" : "VIEWS";
        return statsRepository.findStats(grade, classNo, safeSort).stream()
                .map(ProjectStatsResponse::from)
                .toList();
    }
}