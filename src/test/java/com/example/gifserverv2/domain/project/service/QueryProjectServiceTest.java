package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.project.dto.response.DetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.ListProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.PublicDetailProjectResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.event.ProjectTranslationRequestedEvent;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.global.i18n.Language;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(QueryProjectService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@RecordApplicationEvents
class QueryProjectServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Autowired QueryProjectService service;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectLikeRepository likeRepository;
    @Autowired ApplicationEvents events;

    Long likedProjectId;
    Long otherProjectId;

    @BeforeEach
    void setUp() {
        likedProjectId = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").build()).getId();
        otherProjectId = projectRepository.save(Project.builder().name("VOID").teamName("보이드").build()).getId();
        likeRepository.save(new ProjectLike(likedProjectId, USER_ID));
        likeRepository.save(new ProjectLike(likedProjectId, OTHER_USER_ID));
    }

    @AfterEach
    void tearDown() {
        likeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void 목록_응답에_좋아요_수와_내_좋아요_여부가_포함된다() {
        List<ListProjectResponse> result = service.getAllProjects(USER_ID);

        assertThat(result)
                .extracting(ListProjectResponse::id, ListProjectResponse::likeCount, ListProjectResponse::isLiked)
                .containsExactlyInAnyOrder(
                        tuple(likedProjectId, 2L, true),
                        tuple(otherProjectId, 0L, false));
    }

    @Test
    void 좋아요를_누르지_않은_사용자는_isLiked가_false다() {
        assertThat(service.getAllProjects(3L))
                .extracting(ListProjectResponse::id, ListProjectResponse::likeCount, ListProjectResponse::isLiked)
                .containsExactlyInAnyOrder(
                        tuple(likedProjectId, 2L, false),
                        tuple(otherProjectId, 0L, false));
    }

    @Test
    void 상세_응답에_좋아요_수와_내_좋아요_여부가_포함된다() {
        DetailProjectResponse liked = service.getProject(likedProjectId, USER_ID, Language.KO);
        DetailProjectResponse notLiked = service.getProject(otherProjectId, USER_ID, Language.KO);

        assertThat(liked.likeCount()).isEqualTo(2);
        assertThat(liked.isLiked()).isTrue();
        assertThat(notLiked.likeCount()).isZero();
        assertThat(notLiked.isLiked()).isFalse();
    }

    @Test
    void 좋아요_여부는_isLiked_키로_직렬화된다() throws Exception {
        String json = new ObjectMapper().writeValueAsString(service.getProject(likedProjectId, USER_ID, Language.KO));

        assertThat(json).contains("\"likeCount\":2").contains("\"isLiked\":true");
    }

    private Long saveDescribedProject(String description) {
        return projectRepository.save(Project.builder()
                .name("GIF").teamName("더모먼트").description(description).build()).getId();
    }

    private void saveJapanese(Long projectId, String descriptionJa) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.updateDescriptionJa(descriptionJa, project.descriptionHash());
        projectRepository.save(project);
    }

    private long translationRequests() {
        return events.stream(ProjectTranslationRequestedEvent.class).count();
    }

    @Test
    void 일본어로_조회하면_저장된_번역을_준다() {
        Long projectId = saveDescribedProject("축제 운영 플랫폼입니다.");
        saveJapanese(projectId, "フェスティバル運営プラットフォームです。");

        DetailProjectResponse detail = service.getProject(projectId, USER_ID, Language.JA);
        PublicDetailProjectResponse publicDetail = service.getPublicProject(projectId, null, Language.JA);

        assertThat(detail.description()).isEqualTo("フェスティバル運営プラットフォームです。");
        assertThat(detail.descriptionTranslated()).isTrue();
        assertThat(detail.name()).isEqualTo("GIF");
        assertThat(detail.teamName()).isEqualTo("더모먼트");
        assertThat(publicDetail.description()).isEqualTo("フェスティバル運営プラットフォームです。");
        assertThat(publicDetail.descriptionTranslated()).isTrue();
        assertThat(translationRequests()).isZero();
    }

    @Test
    void 한국어로_조회하면_번역이_있어도_원문을_준다() {
        Long projectId = saveDescribedProject("축제 운영 플랫폼입니다.");
        saveJapanese(projectId, "フェスティバル運営プラットフォームです。");

        DetailProjectResponse detail = service.getProject(projectId, USER_ID, Language.KO);

        assertThat(detail.description()).isEqualTo("축제 운영 플랫폼입니다.");
        assertThat(detail.descriptionTranslated()).isFalse();
        assertThat(translationRequests()).isZero();
    }

    @Test
    void 번역이_없으면_원문을_주고_번역을_요청한다() {
        Long projectId = saveDescribedProject("축제 운영 플랫폼입니다.");

        PublicDetailProjectResponse detail = service.getPublicProject(projectId, null, Language.JA);

        assertThat(detail.description()).isEqualTo("축제 운영 플랫폼입니다.");
        assertThat(detail.descriptionTranslated()).isFalse();
        assertThat(events.stream(ProjectTranslationRequestedEvent.class))
                .containsExactly(new ProjectTranslationRequestedEvent(projectId));
    }

    @Test
    void 소개가_바뀌면_예전_번역은_쓰지_않는다() {
        Long projectId = saveDescribedProject("축제 운영 플랫폼입니다.");
        saveJapanese(projectId, "フェスティバル運営プラットフォームです。");
        Project project = projectRepository.findById(projectId).orElseThrow();
        project.updateDescription("새로운 소개입니다.");
        projectRepository.save(project);

        DetailProjectResponse detail = service.getProject(projectId, USER_ID, Language.JA);

        assertThat(detail.description()).isEqualTo("새로운 소개입니다.");
        assertThat(detail.descriptionTranslated()).isFalse();
        assertThat(translationRequests()).isEqualTo(1);
    }

    @Test
    void 소개가_없으면_번역을_요청하지_않는다() {
        DetailProjectResponse detail = service.getProject(otherProjectId, USER_ID, Language.JA);

        assertThat(detail.description()).isNull();
        assertThat(detail.descriptionTranslated()).isFalse();
        assertThat(translationRequests()).isZero();
    }
}
