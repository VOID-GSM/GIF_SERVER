package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.*;
import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.engagement.entity.ProjectViewCount;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ProjectEngagementService {

    private static final Duration VIEW_DEDUP_TTL = Duration.ofMinutes(30);
    private static final Duration COMMENT_RATE_WINDOW = Duration.ofMinutes(1);
    private static final int COMMENT_RATE_LIMIT = 5;
    private static final int MAX_PAGE_SIZE = 50;

    private final ProjectRepository projectRepository;
    private final ProjectLikeRepository likeRepository;
    private final ProjectCommentRepository commentRepository;
    private final ProjectViewCountRepository viewCountRepository;
    private final AuthorNameResolver authorNameResolver;
    private final StringRedisTemplate redisTemplate;

    // ---------- 반응 수 ----------
    @Transactional(readOnly = true)
    public EngagementResponse getEngagement(Long projectId, Long userIdOrNull) {
        validateProject(projectId);
        boolean likedByMe = userIdOrNull != null
                && likeRepository.existsByProjectIdAndUserId(projectId, userIdOrNull);
        return new EngagementResponse(
                currentViews(projectId),
                likeRepository.countByProjectId(projectId),
                commentRepository.countByProjectId(projectId),
                likedByMe
        );
    }

    // ---------- 좋아요 ----------
    @Transactional
    public LikeResponse toggleLike(Long projectId, Long userId) {
        validateProject(projectId);
        var existing = likeRepository.findByProjectIdAndUserId(projectId, userId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            likeRepository.flush();
            return new LikeResponse(false, likeRepository.countByProjectId(projectId));
        }
        return like(projectId, userId);
    }

    /** DELETE /like 용: 이미 취소된 상태여도 에러 없이 현재 상태 반환 */
    @Transactional
    public LikeResponse unlike(Long projectId, Long userId) {
        validateProject(projectId);
        likeRepository.findByProjectIdAndUserId(projectId, userId).ifPresent(l -> {
            likeRepository.delete(l);
            likeRepository.flush();
        });
        return new LikeResponse(false, likeRepository.countByProjectId(projectId));
    }

    private LikeResponse like(Long projectId, Long userId) {
        try {
            likeRepository.saveAndFlush(new ProjectLike(projectId, userId));
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 이미 좋아요가 생긴 경우: 유니크 제약이 중복을 막아주므로 그대로 성공 처리
        }
        return new LikeResponse(true, likeRepository.countByProjectId(projectId));
    }

    // ---------- 조회수 ----------
    /** viewerKey: 로그인 사용자는 "u:{userId}", 비로그인은 "ip:{ip}" */
    @Transactional
    public ViewResponse increaseView(Long projectId, String viewerKey) {
        validateProject(projectId);
        String key = "project:view:" + projectId + ":" + viewerKey;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", VIEW_DEDUP_TTL);
        if (Boolean.TRUE.equals(first)) {
            viewCountRepository.increase(projectId);
        }
        return new ViewResponse(currentViews(projectId));
    }

    private long currentViews(Long projectId) {
        return viewCountRepository.findById(projectId).map(ProjectViewCount::getViews).orElse(0L);
    }

    // ---------- 댓글 ----------
    @Transactional(readOnly = true)
    public CommentPageResponse getComments(Long projectId, int page, int size) {
        validateProject(projectId);
        int safePage = Math.max(page, 1) - 1; // 프론트는 1부터 시작
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var result = commentRepository
                .findByProjectIdOrderByCreatedAtDesc(projectId, PageRequest.of(safePage, safeSize))
                .map(CommentResponse::from);
        return CommentPageResponse.from(result);
    }

    @Transactional
    public CommentResponse createComment(Long projectId, Long userId, CreateCommentRequest request) {
        validateProject(projectId);
        checkCommentRateLimit(userId);
        String content = request.content().trim();
        if (content.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "댓글 내용은 비어 있을 수 없습니다.");
        }
        var saved = commentRepository.save(
                new ProjectComment(projectId, userId, authorNameResolver.nameOf(userId), content));
        return CommentResponse.from(saved);
    }

    @Transactional
    public void deleteComment(Long projectId, Long commentId, Long userId, boolean isAdmin) {
        var comment = commentRepository.findById(commentId)
                .filter(c -> c.getProjectId().equals(projectId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        if (!isAdmin && !comment.isOwnedBy(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글을 삭제할 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }

    private void checkCommentRateLimit(Long userId) {
        String key = "project:comment:rate:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, COMMENT_RATE_WINDOW);
        }
        if (count != null && count > COMMENT_RATE_LIMIT) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "댓글을 너무 자주 작성하고 있습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private void validateProject(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다.");
        }
    }
}