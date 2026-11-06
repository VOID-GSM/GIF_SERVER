package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.engagement.entity.ProjectViewCount;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewCountRepository;
import com.example.gifserverv2.domain.project.dto.response.DetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.ListProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.PublicDetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.UserSearchResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.project.repository.UserSearchRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;
    private final ProjectLikeRepository likeRepository;
    private final ProjectViewCountRepository viewCountRepository;
    private final ProjectCommentRepository commentRepository; // 추가: 댓글 수 조회용

    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(String keyword) {
        List<UserEntity> users = userSearchRepository.findByNameContainingOrStudentNumberContaining(keyword, keyword);
        if (users.isEmpty()) {
            return List.of();
        }

        List<Long> userIds = users.stream().map(UserEntity::getId).toList();
        Set<Long> userIdsWithTeam = projectMemberRepository.findAllByUserIdIn(userIds).stream()
                .map(ProjectMember::getUserId)
                .collect(Collectors.toSet());

        return users.stream()
                .map(user -> UserSearchResponse.from(user, userIdsWithTeam.contains(user.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ListProjectResponse> getAllProjects(Long userId) {
        return toListResponses(projectRepository.findAll(), userId);
    }

    @Transactional(readOnly = true)
    public List<ListProjectResponse> getMyProjects(Long userId) {
        List<Project> projects = projectMemberRepository.findAllByUserId(userId).stream()
                .map(ProjectMember::getProject)
                .toList();
        return toListResponses(projects, userId);
    }

    // userId 가 null 이면 비로그인 요청이다. (좋아요 여부는 항상 false)
    @Transactional(readOnly = true)
    public List<ListProjectResponse> getProjectsByGrade(Integer grade, Long userId) {
        if (grade == null) return getAllProjects(userId);
        return toListResponses(projectRepository.findByGrade(grade), userId);
    }

    // 교내 사용자용 상세 (기존 응답 + commentCount)
    @Transactional(readOnly = true)
    public DetailProjectResponse getProject(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        return DetailProjectResponse.from(
                project,
                findMemberUsers(project),
                likeRepository.countByProjectId(projectId),
                likeRepository.existsByProjectIdAndUserId(projectId, userId),
                currentViews(projectId),
                commentRepository.countByProjectIdAndHiddenFalseAndDeletedAtIsNull(projectId)
        );
    }

    // 비로그인/외부 사용자용 상세. 학번, userId 를 응답에 담지 않는다.
    @Transactional(readOnly = true)
    public PublicDetailProjectResponse getPublicProject(Long projectId, Long userId) {
        Project project = getProjectOrThrow(projectId);
        boolean liked = userId != null && likeRepository.existsByProjectIdAndUserId(projectId, userId);
        return PublicDetailProjectResponse.from(
                project,
                findMemberUsers(project),
                likeRepository.countByProjectId(projectId),
                liked,
                currentViews(projectId),
                commentRepository.countByProjectIdAndHiddenFalseAndDeletedAtIsNull(projectId)
        );
    }

    private Map<Long, UserEntity> findMemberUsers(Project project) {
        List<Long> memberIds = project.getMembers().stream()
                .map(ProjectMember::getUserId)
                .toList();
        return userRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, u -> u));
    }

    // 조회 기록이 아직 없는 프로젝트는 0으로 처리
    private long currentViews(Long projectId) {
        return viewCountRepository.findById(projectId).map(ProjectViewCount::getViews).orElse(0L);
    }

    private List<ListProjectResponse> toListResponses(List<Project> projects, Long userId) {
        if (projects.isEmpty()) {
            return List.of();
        }

        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        Map<Long, Long> likeCounts = likeRepository.countByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(ProjectLikeRepository.ProjectLikeCount::getProjectId,
                        ProjectLikeRepository.ProjectLikeCount::getLikeCount));
        // 비로그인이면 좋아요 여부 조회를 건너뛴다.
        Set<Long> likedProjectIds = userId == null
                ? Set.of()
                : new HashSet<>(likeRepository.findLikedProjectIds(userId, projectIds));
        // 프로젝트마다 따로 조회하지 않고 쿼리 1번으로 전체를 가져온다 (N+1 방지)
        Map<Long, Long> viewCounts = viewCountRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(ProjectViewCount::getProjectId, ProjectViewCount::getViews));
        Map<Long, Long> commentCounts = commentRepository.countByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(ProjectCommentRepository.ProjectCommentCount::getProjectId,
                        ProjectCommentRepository.ProjectCommentCount::getCommentCount));

        return projects.stream()
                .map(p -> ListProjectResponse.from(
                        p,
                        likeCounts.getOrDefault(p.getId(), 0L),
                        likedProjectIds.contains(p.getId()),
                        viewCounts.getOrDefault(p.getId(), 0L),
                        commentCounts.getOrDefault(p.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Project getProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectException::notFound);
    }
}