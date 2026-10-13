package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.*;
import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.engagement.entity.ProjectView;
import com.example.gifserverv2.domain.engagement.entity.ProjectViewCount;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewCountRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewRepository;
import com.example.gifserverv2.domain.project.exception.ProjectException;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

/**
 * 좋아요·조회수 쓰기 메서드는 의도적으로 @Transactional 을 붙이지 않는다.
 * 동시 요청으로 유니크 제약 위반이 발생했을 때 바깥 트랜잭션이 rollback-only 로 표시되지 않고
 * 예외를 잡아 정상 응답할 수 있도록, 각 쿼리를 리포지토리 단위 트랜잭션으로 실행한다.
 */
@Service
@RequiredArgsConstructor
public class ProjectEngagementService {

    private static final Duration VIEW_DEDUP_WINDOW = Duration.ofMinutes(30);
    private static final Duration COMMENT_RATE_WINDOW = Duration.ofMinutes(1);
    private static final int COMMENT_RATE_LIMIT = 5;
    private static final int MAX_PAGE_SIZE = 50;

    private final ProjectRepository projectRepository;
    private final ProjectLikeRepository likeRepository;
    private final ProjectCommentRepository commentRepository;
    private final ProjectViewRepository viewRepository;
    private final ProjectViewCountRepository viewCountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public EngagementResponse getEngagement(Long projectId, Long userId) {
        validateProject(projectId);
        return new EngagementResponse(
                currentViews(projectId),
                likeRepository.countByProjectId(projectId),
                commentRepository.countByProjectId(projectId),
                likeRepository.existsByProjectIdAndUserId(projectId, userId)
        );
    }

    public LikeResponse toggleLike(Long projectId, Long userId) {
        validateProject(projectId);
        boolean liked = likeRepository.deleteByProjectIdAndUserId(projectId, userId) == 0;
        if (liked) {
            try {
                likeRepository.saveAndFlush(new ProjectLike(projectId, userId));
            } catch (DataIntegrityViolationException e) {
                // 동시 요청이 먼저 좋아요를 저장한 경우: 유니크 제약이 중복을 막았으므로 좋아요 상태로 응답
            }
        }
        return new LikeResponse(liked, likeRepository.countByProjectId(projectId));
    }

    /** 이미 취소된 상태여도 에러 없이 현재 상태를 반환한다. */
    public LikeResponse unlike(Long projectId, Long userId) {
        validateProject(projectId);
        likeRepository.deleteByProjectIdAndUserId(projectId, userId);
        return new LikeResponse(false, likeRepository.countByProjectId(projectId));
    }

    /** 같은 사용자의 조회는 VIEW_DEDUP_WINDOW 동안 한 번만 집계한다. */
    public ViewResponse increaseView(Long projectId, Long userId) {
        validateProject(projectId);
        if (markViewed(projectId, userId)) {
            incrementViews(projectId);
        }
        return new ViewResponse(currentViews(projectId));
    }

    /** 이번 조회를 집계해야 하면 true */
    private boolean markViewed(Long projectId, Long userId) {
        Instant now = Instant.now();
        if (viewRepository.refreshIfExpired(projectId, userId, now, now.minus(VIEW_DEDUP_WINDOW)) > 0) {
            return true;
        }
        if (viewRepository.existsByProjectIdAndUserId(projectId, userId)) {
            return false;
        }
        try {
            viewRepository.saveAndFlush(new ProjectView(projectId, userId, now));
            return true;
        } catch (DataIntegrityViolationException e) {
            // 동시 요청이 먼저 첫 조회를 기록한 경우
            return false;
        }
    }

    private void incrementViews(Long projectId) {
        if (viewCountRepository.increment(projectId) > 0) {
            return;
        }
        try {
            viewCountRepository.saveAndFlush(new ProjectViewCount(projectId, 1L));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청이 먼저 카운터 행을 만든 경우
            viewCountRepository.increment(projectId);
        }
    }

    private long currentViews(Long projectId) {
        return viewCountRepository.findById(projectId).map(ProjectViewCount::getViews).orElse(0L);
    }

    @Transactional(readOnly = true)
    public CommentPageResponse getComments(Long projectId, int page, int size) {
        validateProject(projectId);
        int safePage = Math.max(page, 1) - 1; // 프론트는 1부터 시작
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return CommentPageResponse.from(commentRepository
                .findByProjectIdOrderByCreatedAtDescIdDesc(projectId, PageRequest.of(safePage, safeSize))
                .map(CommentResponse::from));
    }

    @Transactional
    public CommentResponse createComment(Long projectId, Long userId, CreateCommentRequest request) {
        validateProject(projectId);
        checkCommentRateLimit(userId);
        String authorName = userRepository.findById(userId)
                .map(UserEntity::getName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        ProjectComment saved = commentRepository.save(
                new ProjectComment(projectId, userId, authorName, request.content().trim()));
        return CommentResponse.from(saved);
    }

    /** 작성자 본인 또는 관리자만 삭제할 수 있다. */
    @Transactional
    public void deleteComment(Long projectId, Long commentId, Long userId, boolean isAdmin) {
        ProjectComment comment = commentRepository.findById(commentId)
                .filter(c -> c.getProjectId().equals(projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!isAdmin && !comment.isOwnedBy(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    private void checkCommentRateLimit(Long userId) {
        long recent = commentRepository.countByUserIdAndCreatedAtAfter(userId, Instant.now().minus(COMMENT_RATE_WINDOW));
        if (recent >= COMMENT_RATE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "댓글을 너무 자주 작성하고 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void validateProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw ProjectException.notFound();
        }
    }
}
