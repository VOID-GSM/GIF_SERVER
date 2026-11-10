package com.example.gifserverv2.domain.engagement.service;

import com.example.gifserverv2.domain.engagement.dto.request.CreateCommentRequest;
import com.example.gifserverv2.domain.engagement.dto.response.CommentPageResponse;
import com.example.gifserverv2.domain.engagement.dto.response.CommentResponse;
import com.example.gifserverv2.domain.engagement.dto.response.EngagementResponse;
import com.example.gifserverv2.domain.engagement.dto.response.LikeResponse;
import com.example.gifserverv2.domain.engagement.entity.ProjectComment;
import com.example.gifserverv2.domain.engagement.repository.ProjectCommentRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewCountRepository;
import com.example.gifserverv2.domain.engagement.repository.ProjectViewRepository;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(ProjectEngagementService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProjectEngagementServiceTest {

    @Autowired ProjectEngagementService service;
    @Autowired ProjectRepository projectRepository;
    @Autowired UserRepository userRepository;
    @Autowired ProjectLikeRepository likeRepository;
    @Autowired ProjectCommentRepository commentRepository;
    @Autowired ProjectViewRepository viewRepository;
    @Autowired ProjectViewCountRepository viewCountRepository;

    Long projectId;
    Long userId;
    Long otherUserId;

    @BeforeEach
    void setUp() {
        projectId = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").build()).getId();
        userId = userRepository.save(new UserEntity("a@gsm.hs.kr", "홍길동", "2101", Role.USER)).getId();
        otherUserId = userRepository.save(new UserEntity("b@gsm.hs.kr", "김철수", "2102", Role.USER)).getId();
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        commentRepository.deleteAll();
        viewRepository.deleteAll();
        viewCountRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 좋아요는_토글되고_사용자별로_한_번만_집계된다() {
        assertThat(service.toggleLike(projectId, userId)).isEqualTo(new LikeResponse(true, 1));
        assertThat(service.toggleLike(projectId, otherUserId)).isEqualTo(new LikeResponse(true, 2));
        assertThat(service.getEngagement(projectId, userId).likedByMe()).isTrue();

        assertThat(service.toggleLike(projectId, userId)).isEqualTo(new LikeResponse(false, 1));
        assertThat(service.getEngagement(projectId, userId).likedByMe()).isFalse();
    }

    @Test
    void 좋아요_취소는_이미_취소된_상태여도_성공한다() {
        service.toggleLike(projectId, userId);

        assertThat(service.unlike(projectId, userId)).isEqualTo(new LikeResponse(false, 0));
        assertThat(service.unlike(projectId, userId)).isEqualTo(new LikeResponse(false, 0));
    }

    @Test
    void 조회수는_같은_사용자의_반복_요청을_중복_집계하지_않는다() {
        assertThat(service.increaseView(projectId, userId).views()).isEqualTo(1);
        assertThat(service.increaseView(projectId, userId).views()).isEqualTo(1);
        assertThat(service.increaseView(projectId, otherUserId).views()).isEqualTo(2);
    }

    @Test
    void 조회수는_중복_방지_시간이_지나면_다시_집계된다() {
        service.increaseView(projectId, userId);
        Instant now = Instant.now();
        viewRepository.refreshIfExpired(projectId, userId, now.minus(1, ChronoUnit.HOURS), now.plusSeconds(1));

        assertThat(service.increaseView(projectId, userId).views()).isEqualTo(2);
        assertThat(service.increaseView(projectId, userId).views()).isEqualTo(2);
    }

    @Test
    void 동시에_들어온_조회_요청은_에러_없이_한_번만_집계된다() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Object>> tasks = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tasks.add(() -> service.increaseView(projectId, userId));
            }
            for (Future<Object> f : pool.invokeAll(tasks)) {
                f.get();
            }
        } finally {
            pool.shutdown();
        }

        assertThat(service.getEngagement(projectId, userId).views()).isEqualTo(1);
    }

    @Test
    void 댓글을_작성하면_목록과_개수에_반영된다() {
        CommentResponse created = service.createComment(projectId, userId, new CreateCommentRequest("  멋진 프로젝트네요!  "));
        service.createComment(projectId, otherUserId, new CreateCommentRequest("두 번째"));

        assertThat(created.content()).isEqualTo("멋진 프로젝트네요!");
        assertThat(created.author()).isEqualTo(new CommentResponse.Author(userId, "홍길동", null));
        assertThat(created.isMine()).isTrue(); // 방금 작성한 댓글은 본인 댓글

        CommentPageResponse firstPage = service.getComments(projectId, userId, 1, 1);
        assertThat(firstPage.totalElements()).isEqualTo(2);
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.content()).extracting(CommentResponse::content).containsExactly("두 번째");
        assertThat(service.getComments(projectId, userId, 2, 1).hasNext()).isFalse();

        EngagementResponse engagement = service.getEngagement(projectId, userId);
        assertThat(engagement.commentsCount()).isEqualTo(2);
    }

    @Test
    void 커서로_이어서_조회하면_사이에_댓글이_등록_삭제돼도_중복_누락이_없다() {
        Instant base = Instant.parse("2026-11-10T00:00:00Z");
        // c1 이 가장 오래됨. c3·c4 는 작성 시각이 같아 ID 로 순서가 정해진다
        ProjectComment c1 = saveComment("c1", base);
        ProjectComment c2 = saveComment("c2", base.plusSeconds(1));
        ProjectComment c3 = saveComment("c3", base.plusSeconds(2));
        ProjectComment c4 = saveComment("c4", base.plusSeconds(2));
        ProjectComment c5 = saveComment("c5", base.plusSeconds(3));

        CommentPageResponse first = service.getComments(projectId, userId, 1, 2);
        assertThat(first.content()).extracting(CommentResponse::content).containsExactly("c5", "c4");
        assertThat(first.nextCursor()).isEqualTo(c4.getId());

        // 더보기 전에 새 댓글이 달리고, 첫 페이지의 c4 와 다음 페이지 첫 댓글 c3 이 삭제된다
        saveComment("new", base.plusSeconds(10));
        c4.softDelete();
        c3.softDelete();
        commentRepository.saveAll(List.of(c4, c3));

        CommentPageResponse second = service.getCommentsAfter(projectId, userId, first.nextCursor(), 2);
        assertThat(second.content()).extracting(CommentResponse::content).containsExactly("c2", "c1");
        assertThat(second.hasNext()).isFalse();
        assertThat(second.nextCursor()).isNull();
        assertThat(second.totalElements()).isEqualTo(4); // new, c5, c2, c1
    }

    @Test
    void 커서_조회는_다음_페이지가_있으면_마지막_댓글_ID를_커서로_준다() {
        Instant base = Instant.parse("2026-11-10T00:00:00Z");
        saveComment("c1", base);
        ProjectComment c2 = saveComment("c2", base.plusSeconds(1));
        ProjectComment c3 = saveComment("c3", base.plusSeconds(2));

        CommentPageResponse page = service.getCommentsAfter(projectId, userId, c3.getId(), 1);

        assertThat(page.content()).extracting(CommentResponse::content).containsExactly("c2");
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).isEqualTo(c2.getId());
    }

    @Test
    void 없는_댓글이나_다른_프로젝트_댓글을_커서로_보내면_400() {
        Long otherProjectId = projectRepository.save(Project.builder().name("other").teamName("다른팀").build()).getId();
        ProjectComment others = commentRepository.save(new ProjectComment(otherProjectId, userId, "홍길동", "다른 프로젝트"));

        assertThatThrownBy(() -> service.getCommentsAfter(projectId, userId, others.getId(), 10))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThatThrownBy(() -> service.getCommentsAfter(projectId, userId, others.getId() + 1000, 10))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private ProjectComment saveComment(String content, Instant createdAt) {
        ProjectComment comment = new ProjectComment(projectId, userId, "홍길동", content);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        return commentRepository.save(comment);
    }

    @Test
    void 댓글은_작성자와_관리자만_삭제할_수_있다() {
        Long first = service.createComment(projectId, userId, new CreateCommentRequest("첫 댓글")).id();
        Long second = service.createComment(projectId, userId, new CreateCommentRequest("둘째 댓글")).id();

        assertThatThrownBy(() -> service.deleteComment(projectId, first, otherUserId, false))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN));

        service.deleteComment(projectId, first, userId, false);
        service.deleteComment(projectId, second, otherUserId, true);
        assertThat(service.getEngagement(projectId, userId).commentsCount()).isZero();
    }

    @Test
    void 댓글을_짧은_시간에_너무_많이_작성하면_거절된다() {
        for (int i = 0; i < 5; i++) {
            service.createComment(projectId, userId, new CreateCommentRequest("댓글 " + i));
        }

        assertThatThrownBy(() -> service.createComment(projectId, userId, new CreateCommentRequest("여섯 번째")))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
        service.createComment(projectId, otherUserId, new CreateCommentRequest("다른 사용자"));
    }

    @Test
    void 존재하지_않는_프로젝트는_404를_반환한다() {
        assertThatThrownBy(() -> service.toggleLike(999_999L, userId))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
