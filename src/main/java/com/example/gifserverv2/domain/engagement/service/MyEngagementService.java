package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.engagement.dto.response.MyCommentPageResponse;
import com.example.gifserverv2.domain.engagement.dto.response.MyCommentResponse;
import com.example.gifserverv2.domain.engagement.dto.response.MyLikedProjectPageResponse;
import com.example.gifserverv2.domain.engagement.dto.response.MyLikedProjectResponse;
import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 마이페이지의 내가 쓴 댓글·좋아요한 프로젝트 목록 (#248). 학교 사용자·외부 사용자 모두 사용한다.
 */
@Service
@RequiredArgsConstructor
public class MyEngagementService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProjectCommentRepository commentRepository;
    private final ProjectLikeRepository likeRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public MyCommentPageResponse getMyComments(Long userId, int page, int size) {
        Page<ProjectComment> comments = commentRepository.findMine(userId, pageRequest(page, size));
        Map<Long, Project> projects = findProjects(comments.map(ProjectComment::getProjectId).toSet());
        return MyCommentPageResponse.from(comments.map(c -> MyCommentResponse.from(c, projects.get(c.getProjectId()))));
    }

    @Transactional(readOnly = true)
    public MyLikedProjectPageResponse getMyLikes(Long userId, int page, int size) {
        Page<ProjectLike> likes = likeRepository.findMine(userId, pageRequest(page, size));
        Set<Long> projectIds = likes.map(ProjectLike::getProjectId).toSet();
        Map<Long, Project> projects = findProjects(projectIds);
        // 좋아요 수를 프로젝트 묶음으로 한 번에 집계한다. (프로젝트마다 따로 세면 N+1)
        Map<Long, Long> likeCounts = projectIds.isEmpty() ? Map.of() : likeRepository.countByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(ProjectLikeRepository.ProjectLikeCount::getProjectId,
                        ProjectLikeRepository.ProjectLikeCount::getLikeCount));
        return MyLikedProjectPageResponse.from(likes.map(l -> MyLikedProjectResponse.from(
                l, projects.get(l.getProjectId()), likeCounts.getOrDefault(l.getProjectId(), 0L))));
    }

    private Map<Long, Project> findProjects(Collection<Long> projectIds) {
        return projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, Function.identity()));
    }

    private static PageRequest pageRequest(int page, int size) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return PageRequest.of(safePage, safeSize);
    }
}
