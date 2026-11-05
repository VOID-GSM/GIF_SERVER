package com.example.gifserverv2.domain.project.service;

import com.example.gifserverv2.domain.engagement.entity.ProjectLike;
import com.example.gifserverv2.domain.engagement.repository.ProjectLikeRepository;
import com.example.gifserverv2.domain.project.dto.response.DetailProjectResponse;
import com.example.gifserverv2.domain.project.dto.response.ListProjectResponse;
import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
class QueryProjectServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    @Autowired QueryProjectService service;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectLikeRepository likeRepository;

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
        DetailProjectResponse liked = service.getProject(likedProjectId, USER_ID);
        DetailProjectResponse notLiked = service.getProject(otherProjectId, USER_ID);

        assertThat(liked.likeCount()).isEqualTo(2);
        assertThat(liked.isLiked()).isTrue();
        assertThat(notLiked.likeCount()).isZero();
        assertThat(notLiked.isLiked()).isFalse();
    }

    @Test
    void 좋아요_여부는_isLiked_키로_직렬화된다() throws Exception {
        String json = new ObjectMapper().writeValueAsString(service.getProject(likedProjectId, USER_ID));

        assertThat(json).contains("\"likeCount\":2").contains("\"isLiked\":true");
    }
}
