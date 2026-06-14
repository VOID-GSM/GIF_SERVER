package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.global.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;
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

        if (request.getAddMemberIds() != null || request.getRemoveMemberIds() != null) {
            List<ProjectMember> currentMembers = projectMemberRepository.findAllByProjectId(projectId);
            Map<Long, ProjectMember> memberMap = currentMembers.stream()
                    .collect(Collectors.toMap(ProjectMember::getUserId, member -> member));

            if (request.getAddMemberIds() != null) {
                request.getAddMemberIds().forEach(memberId -> {
                    if (memberMap.containsKey(memberId)) {
                        throw ProjectException.alreadyMember();
                    }
                    ProjectMember newMember = ProjectMember.builder()
                            .project(project)
                            .userId(memberId)
                            .role(ProjectMember.MemberRole.MEMBER)
                            .build();
                    projectMemberRepository.save(newMember);
                    memberMap.put(memberId, newMember);
                });
            }

            if (request.getRemoveMemberIds() != null) {
                request.getRemoveMemberIds().forEach(memberId -> {
                    ProjectMember member = memberMap.get(memberId);
                    if (member == null) {
                        throw ProjectException.notMember();
                    }
                    if (member.getRole() == ProjectMember.MemberRole.LEADER) {
                        throw ProjectException.cannotRemoveLeader();
                    }
                    projectMemberRepository.delete(member);
                    memberMap.remove(memberId);
                });
            }
        }
    }

    public Long createProject(Long userId, CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.getName())
                .teamName(request.getTeamName())
                .description(request.getDescription())
                .build();

        Project savedProject = projectRepository.save(project);

        if (request.getLogo() != null && !request.getLogo().isEmpty()) {
            String logoUrl = projectLogoStorageService.save(request.getLogo());
            savedProject.updateLogoPath(logoUrl);
        }

        ProjectMember leader = ProjectMember.builder()
                .project(savedProject)
                .userId(userId)
                .role(ProjectMember.MemberRole.LEADER)
                .build();
        projectMemberRepository.save(leader);

        if (request.getMemberIds() != null) {
            for (Long memberId : request.getMemberIds()) {
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