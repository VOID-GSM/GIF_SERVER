package com.example.gifserverv2.domain.project.service;


import com.example.gifserverv2.domain.project.dto.request.*;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommandProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;

    public Long createProject(Long leaderId, CreateProjectRequest request) {
        Project project = Project.builder()
                .name(request.name())
                .teamName(request.teamName())
                .description(request.description())
                .build();
        projectRepository.save(project);

        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .userId(leaderId)
                .role(ProjectMember.MemberRole.LEADER)
                .build());

        if (request.memberIds() != null) {
            request.memberIds().stream()
                    .filter(id -> !id.equals(leaderId))
                    .forEach(memberId ->
                            projectMemberRepository.save(ProjectMember.builder()
                                    .project(project)
                                    .userId(memberId)
                                    .role(ProjectMember.MemberRole.MEMBER)
                                    .build())
                    );
        }

        return project.getId();
    }

    public void updateName(Long projectId, Long userId, UpdateNameProjectRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateName(request.name());
    }

    public void updateTeamName(Long projectId, Long userId, UpdateTeamNameProjectRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateTeamName(request.teamName());
    }

    public void updateDescription(Long projectId, Long userId, UpdateDescriptionProjectRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);
        project.updateDescription(request.description());
    }

    public void updateMembers(Long projectId, Long userId, UpdateMembersProjectRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);
        validateLeader(projectId, userId);

        if (request.addMemberIds() != null) {
            request.addMemberIds().forEach(memberId -> {
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

        if (request.removeMemberIds() != null) {
            request.removeMemberIds().forEach(memberId -> {
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

    private void validateLeader(Long projectId, Long userId) {
        ProjectMember member = projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(ProjectException::notMember);

        if (member.getRole() != ProjectMember.MemberRole.LEADER) {
            throw ProjectException.notLeader();
        }
    }
}
