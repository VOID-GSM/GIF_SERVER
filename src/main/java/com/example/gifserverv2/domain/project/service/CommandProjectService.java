package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;
    private final FileStorageService fileStorageService;
    private final ProjectLogoStorageService projectLogoStorageService;

    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request, MultipartFile logo) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        if (request.getName() != null) project.updateName(request.getName());
        if (request.getTeamName() != null) project.updateTeamName(request.getTeamName());
        if (request.getDescription() != null) project.updateDescription(request.getDescription());

        if (logo != null && !logo.isEmpty()) {
            replaceLogo(project, logo);
        }

        if (request.getAddMemberIds() != null) {
            request.getAddMemberIds().forEach(memberId -> {
                if (projectMemberRepository.existsByProjectIdAndUserId(projectId, memberId)) {
                    throw ProjectException.alreadyMember();
                }
                projectMemberRepository.save(ProjectMember.builder()
                        .project(project)
                        .userId(memberId)
                        .role(ProjectMember.MemberRole.MEMBER)
                        .build());
            });
        }

        if (request.getRemoveMemberIds() != null) {
            request.getRemoveMemberIds().forEach(memberId -> {
                ProjectMember member = projectMemberRepository
                        .findByProjectIdAndUserId(projectId, memberId)
                        .orElseThrow(ProjectException::notMember);
                if (member.getRole() == ProjectMember.MemberRole.LEADER) {
                    throw ProjectException.cannotRemoveLeader();
                }
                projectMemberRepository.delete(member);
            });
        }
    }
    public Long createProject(Long userId, CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.name())
                .teamName(request.teamName())
                .description(request.description())
                .build();

        Project savedProject = projectRepository.save(project);

        ProjectMember leader = ProjectMember.builder()
                .project(savedProject)
                .userId(userId)
                .role(ProjectMember.MemberRole.LEADER)
                .build();
        projectMemberRepository.save(leader);

        if (request.memberIds() != null) {
            for (Long memberId : request.memberIds()) {
                if (memberId.equals(userId)) continue;

                ProjectMember member = ProjectMember.builder()
                        .project(savedProject)
                        .userId(memberId)
                        .role(ProjectMember.MemberRole.MEMBER)
                        .build();
                projectMemberRepository.save(member);
            }
        }

        return savedProject.getId();
    }
    public void uploadLogo(Long projectId, Long userId, MultipartFile file) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        replaceLogo(project, file);
    }

    private void replaceLogo(Project project, MultipartFile file) {
        String oldLogoUrl = project.getLogoPath();

        String newLogoUrl = projectLogoStorageService.save(file);
        project.updateLogoPath(newLogoUrl);

        if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
            projectLogoStorageService.delete(oldLogoUrl);
        }
    }

    private void validateLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (member.getRole() != ProjectMember.MemberRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }
}