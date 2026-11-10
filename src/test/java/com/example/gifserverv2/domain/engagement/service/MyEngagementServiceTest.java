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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(MyEngagementService.class)
class MyEngagementServiceTest {

    static final Long ME = 1L;
    static final Long OTHER = 2L;

    @Autowired MyEngagementService service;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectCommentRepository commentRepository;
    @Autowired ProjectLikeRepository likeRepository;

    Project gif;
    Project voidProject;

    @BeforeEach
    void setUp() {
        gif = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").logo("/files/project-logos/gif.png").build());
        voidProject = projectRepository.save(Project.builder().name("VOID").teamName("보이드").build());
    }

    @Test
    void 내가_쓴_댓글을_최신순으로_프로젝트_정보와_함께_준다() {
        Instant base = Instant.parse("2026-11-10T00:00:00Z");
        saveComment(gif, ME, "첫 댓글", base);
        ProjectComment hidden = saveComment(voidProject, ME, "숨겨진 댓글", base.plusSeconds(1));
        hidden.hide();
        ProjectComment deleted = saveComment(gif, ME, "지운 댓글", base.plusSeconds(2));
        deleted.softDelete();
        saveComment(gif, OTHER, "남의 댓글", base.plusSeconds(3));
        saveComment(voidProject, ME, "최신 댓글", base.plusSeconds(4));
        commentRepository.flush();

        MyCommentPageResponse page = service.getMyComments(ME, 1, 20);

        assertThat(page.content()).extracting(MyCommentResponse::content)
                .containsExactly("최신 댓글", "숨겨진 댓글", "첫 댓글");
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.hasNext()).isFalse();
        MyCommentResponse latest = page.content().get(0);
        assertThat(latest.projectId()).isEqualTo(voidProject.getId());
        assertThat(latest.projectName()).isEqualTo("VOID");
        assertThat(latest.teamName()).isEqualTo("보이드");
        assertThat(latest.hidden()).isFalse();
        // 관리자가 숨긴 댓글은 숨김 상태로 표시한다
        assertThat(page.content().get(1).hidden()).isTrue();
    }

    @Test
    void 없는_프로젝트의_댓글과_좋아요는_제외한다() {
        Long removedProjectId = voidProject.getId() + 1000;
        commentRepository.save(new ProjectComment(removedProjectId, ME, "홍길동", "사라진 프로젝트"));
        likeRepository.save(new ProjectLike(removedProjectId, ME));
        saveComment(gif, ME, "남은 댓글", Instant.now());
        likeRepository.save(new ProjectLike(gif.getId(), ME));

        MyCommentPageResponse comments = service.getMyComments(ME, 1, 20);
        MyLikedProjectPageResponse likes = service.getMyLikes(ME, 1, 20);

        assertThat(comments.content()).extracting(MyCommentResponse::content).containsExactly("남은 댓글");
        assertThat(comments.totalElements()).isEqualTo(1);
        assertThat(likes.content()).extracting(MyLikedProjectResponse::projectId).containsExactly(gif.getId());
        assertThat(likes.totalElements()).isEqualTo(1);
    }

    @Test
    void 좋아요한_프로젝트를_좋아요_누른_최신순으로_좋아요_수와_함께_준다() {
        LocalDateTime base = LocalDateTime.of(2026, 11, 10, 9, 0);
        saveLike(gif, ME, base);
        saveLike(voidProject, ME, base.plusMinutes(1));
        saveLike(gif, OTHER, base.plusMinutes(2));

        MyLikedProjectPageResponse page = service.getMyLikes(ME, 1, 20);

        assertThat(page.content()).extracting(MyLikedProjectResponse::projectName).containsExactly("VOID", "GIF");
        assertThat(page.totalElements()).isEqualTo(2);
        MyLikedProjectResponse gifLike = page.content().get(1);
        assertThat(gifLike.teamName()).isEqualTo("더모먼트");
        assertThat(gifLike.logo()).isEqualTo("/files/project-logos/gif.png");
        // 다른 사용자의 좋아요까지 포함한 프로젝트 전체 좋아요 수
        assertThat(gifLike.likeCount()).isEqualTo(2);
        // 한국 시간 09:00 은 UTC 00:00 이다
        assertThat(gifLike.likedAt()).isEqualTo(Instant.parse("2026-11-10T00:00:00Z"));
        assertThat(page.content().get(0).likeCount()).isEqualTo(1);
    }

    @Test
    void 페이지_크기만큼_나눠서_주고_범위를_벗어난_페이지_번호는_보정한다() {
        Instant base = Instant.parse("2026-11-10T00:00:00Z");
        for (int i = 0; i < 3; i++) {
            saveComment(gif, ME, "댓글" + i, base.plusSeconds(i));
        }

        MyCommentPageResponse first = service.getMyComments(ME, 0, 2);
        MyCommentPageResponse second = service.getMyComments(ME, 2, 2);

        assertThat(first.content()).extracting(MyCommentResponse::content).containsExactly("댓글2", "댓글1");
        assertThat(first.hasNext()).isTrue();
        assertThat(second.content()).extracting(MyCommentResponse::content).containsExactly("댓글0");
        assertThat(second.hasNext()).isFalse();
        assertThat(service.getMyLikes(ME, 1, 20).content()).isEmpty();
    }

    private ProjectComment saveComment(Project project, Long userId, String content, Instant createdAt) {
        ProjectComment comment = new ProjectComment(project.getId(), userId, "홍길동", content);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        return commentRepository.save(comment);
    }

    private void saveLike(Project project, Long userId, LocalDateTime createdAt) {
        ProjectLike like = new ProjectLike(project.getId(), userId);
        ReflectionTestUtils.setField(like, "createdAt", createdAt);
        likeRepository.save(like);
    }
}
