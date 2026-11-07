package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.engagement.dto.response.AdminCommentPageResponse;
import com.example.gifserverv2.domain.engagement.dto.response.AdminCommentResponse;
import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminCommentService {

    private static final int MAX_PAGE_SIZE = 50;

    private final ProjectCommentRepository commentRepository;

    // 숨김 댓글 포함, 삭제된 댓글 제외. projectId / userId 는 값이 없으면 필터를 적용하지 않는다.
    @Transactional(readOnly = true)
    public AdminCommentPageResponse getComments(Long projectId, Long userId, int page, int size) {
        int safePage = Math.max(page, 1) - 1;
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return AdminCommentPageResponse.from(
                commentRepository.findForAdmin(projectId, userId, pageable).map(AdminCommentResponse::from));
    }

    // 변경 감지(dirty checking)로 트랜잭션이 끝날 때 자동 저장된다.
    @Transactional
    public void hide(Long commentId) {
        findActive(commentId).hide();
    }

    @Transactional
    public void unhide(Long commentId) {
        findActive(commentId).unhide();
    }

    // 행을 지우지 않고 삭제 시각만 기록한다.
    @Transactional
    public void delete(Long commentId) {
        findActive(commentId).softDelete();
    }

    private ProjectComment findActive(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
    }
}