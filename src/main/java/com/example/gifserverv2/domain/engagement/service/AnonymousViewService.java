package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.engagement.dto.response.ViewResponse;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnonymousViewService {

    private final ProjectRepository projectRepository;
    private final AnonymousViewGuard guard;
    private final ProjectEngagementService engagementService;

    public ViewResponse increaseView(Long projectId, String visitorId, String ip, String userAgent) {
        // 없는 프로젝트로 Redis 키가 쌓이지 않도록 판정 전에 먼저 확인한다.
        if (!projectRepository.existsById(projectId)) {
            throw ProjectException.notFound();
        }
        boolean counted = guard.shouldCount(projectId, visitorId, ip, userAgent);
        return engagementService.applyView(projectId, counted);
    }
}