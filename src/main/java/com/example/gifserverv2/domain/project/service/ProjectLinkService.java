package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectLinkRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectLinkRequest;
import com.example.gifserverv2.domain.project.dto.response.ProjectLinkResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectLink;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectLinkRepository;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectLinkService {

    private final ProjectLinkRepository projectLinkRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;

    @Transactional(readOnly = true)
    public List<ProjectLinkResponse> getLinks(Long projectId) {
        return projectLinkRepository.findAllByProjectIdOrderByCreatedAtAsc(projectId).stream()
                .map(ProjectLinkResponse::from)
                .toList();
    }

    @Transactional
    public Long createLink(Long projectId, Long userId, CreateProjectLinkRequest request) {
        validateMember(projectId, userId);
        validateUrl(request.url());

        Project project = projectQueryService.getProjectOrThrow(projectId);

        ProjectLink link = ProjectLink.builder()
                .project(project)
                .title(request.title())
                .url(request.url())
                .build();

        return projectLinkRepository.save(link).getId();
    }

    @Transactional
    public void updateLink(Long projectId, Long linkId, Long userId, UpdateProjectLinkRequest request) {
        validateMember(projectId, userId);
        validateUrl(request.url());

        ProjectLink link = getLinkOrThrow(projectId, linkId);
        link.update(request.title(), request.url());
    }

    @Transactional
    public void deleteLink(Long projectId, Long linkId, Long userId) {
        validateMember(projectId, userId);

        ProjectLink link = getLinkOrThrow(projectId, linkId);
        projectLinkRepository.delete(link);
    }

    private ProjectLink getLinkOrThrow(Long projectId, Long linkId) {
        ProjectLink link = projectLinkRepository.findById(linkId)
                .orElseThrow(ProjectException::linkNotFound);

        if (!link.getProject().getId().equals(projectId)) {
            throw ProjectException.linkNotFound();
        }
        return link;
    }

    private void validateMember(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ProjectException.notMember();
        }
    }

    private void validateUrl(String url) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            throw ProjectException.invalidUrl();
        }
    }
}