package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.project.dto.request.CreateProjectRequest;
import com.example.gifserverv2.domain.project.dto.request.TransferLeaderRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectDescriptionRequest;
import com.example.gifserverv2.domain.project.dto.request.UpdateProjectRequest;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.push.entity.PushMessageTemplate;
import com.example.gifserverv2.domain.push.service.PushSenderService;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandProjectService {

    private final ProjectMemberRepository projectMemberRepository;
    private final QueryProjectService projectQueryService;
    private final ProjectLogoStorageService projectLogoStorageService;
    private final UserRepository userRepository;
    private final PushSenderService pushSenderService;
    private final ProjectWriterService projectWriter;

    public void updateProject(Long projectId, Long userId, UpdateProjectRequest request, MultipartFile logo) {
        ProjectWriterService.ProjectMutationResult coreResult =
                projectWriter.updateCoreFields(projectId, userId, request);

        if (logo != null && !logo.isEmpty()) {
            String oldLogoUrl = projectWriter.getCurrentLogo(projectId);
            String newLogoUrl = projectLogoStorageService.save(logo);
            projectWriter.setLogo(projectId, newLogoUrl);
            if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
                projectLogoStorageService.delete(oldLogoUrl);
            }
        }

        if (request.getAddMemberIds() != null || request.getRemoveMemberIds() != null) {
            ProjectWriterService.MemberReconciliation reconciliation =
                    projectWriter.reconcileMembers(projectId, request.getAddMemberIds(), request.getRemoveMemberIds());

            if (!reconciliation.addedMemberIds().isEmpty()) {
                pushSenderService.sendBulkNotifications(
                        reconciliation.addedMemberIds(),
                        PushMessageTemplate.TEAM_MEMBER_ADDED.getTitle(),
                        PushMessageTemplate.TEAM_MEMBER_ADDED.formatBody(coreResult.projectName())
                );
            }
            if (!reconciliation.removedMemberIds().isEmpty()) {
                pushSenderService.sendBulkNotifications(
                        reconciliation.removedMemberIds(),
                        PushMessageTemplate.TEAM_MEMBER_REMOVED.getTitle(),
                        PushMessageTemplate.TEAM_MEMBER_REMOVED.formatBody(coreResult.projectName())
                );
            }
        }
    }

    public Long createProject(Long userId, CreateProjectRequest request) {
        ProjectWriterService.CreatedProject created = projectWriter.createProject(userId, request);

        if (request.logo() != null && !request.logo().isEmpty()) {
            String logoUrl = projectLogoStorageService.save(request.logo());
            projectWriter.setLogo(created.projectId(), logoUrl);
        }

        List<Long> teacherIds = userRepository.findAllByAdminRoleIsNotNull().stream()
                .map(UserEntity::getId)
                .toList();

        pushSenderService.sendBulkNotifications(
                teacherIds,
                PushMessageTemplate.PROJECT_CREATED.getTitle(),
                PushMessageTemplate.PROJECT_CREATED.formatBody(created.projectName())
        );

        return created.projectId();
    }

    public void uploadLogo(Long projectId, Long userId, MultipartFile file) {
        String oldLogoUrl = projectWriter.prepareLogoUpload(projectId, userId);

        String newLogoUrl = projectLogoStorageService.save(file);
        projectWriter.setLogo(projectId, newLogoUrl);

        if (oldLogoUrl != null && !oldLogoUrl.isBlank()) {
            projectLogoStorageService.delete(oldLogoUrl);
        }
    }

    @Transactional
    public void updateDescription(Long projectId, Long userId, UpdateProjectDescriptionRequest request) {
        Project project = projectQueryService.getProjectOrThrow(projectId);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw ProjectException.notMember();
        }

        if (request != null && request.description() != null) {
            project.updateDescription(request.description());
            project.clearAiSummary();
        }
    }

    public void transferLeader(Long projectId, Long userId, TransferLeaderRequest request) {
        ProjectWriterService.LeaderTransferResult result = projectWriter.transferLeader(projectId, userId, request);

        pushSenderService.sendBulkNotifications(
                result.memberUserIds(),
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.getTitle(),
                PushMessageTemplate.LEADER_TRANSFERRED_CLIENT.formatBody(result.projectName(), result.newLeaderName())
        );

        pushSenderService.sendBulkNotifications(
                result.teacherIds(),
                PushMessageTemplate.LEADER_TRANSFERRED_ADMIN.getTitle(),
                PushMessageTemplate.LEADER_TRANSFERRED_ADMIN.formatBody(result.masterName(), result.projectName(), result.newLeaderName())
        );
    }
}
