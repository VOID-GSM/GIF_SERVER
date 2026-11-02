package com.example.gifserverv2.domain.retrospective.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.entity.ProjectMember;
import com.example.gifserverv2.domain.project.repository.ProjectMemberRepository;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.retrospective.dto.request.CreateRetrospectiveRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RelatedWorkRequest;
import com.example.gifserverv2.domain.retrospective.dto.request.RetrospectivePatchRequest;
import com.example.gifserverv2.domain.retrospective.dto.response.DetailRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.dto.response.ListRetrospectiveResponse;
import com.example.gifserverv2.domain.retrospective.entity.RelatedWorkType;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveCategory;
import com.example.gifserverv2.domain.retrospective.entity.RetrospectiveVisibility;
import com.example.gifserverv2.domain.user.entity.ClientRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(RetrospectiveService.class)
class RetrospectiveServiceTest {

    @Autowired RetrospectiveService service;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectMemberRepository projectMemberRepository;
    @Autowired UserRepository userRepository;

    Long projectId;
    Long authorId;
    Long readerId;

    @BeforeEach
    void setUp() {
        Project project = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").build());
        projectId = project.getId();
        authorId = userRepository.save(new UserEntity("a@gsm.hs.kr", "홍길동", "2101", Role.USER)).getId();
        readerId = userRepository.save(new UserEntity("b@gsm.hs.kr", "김철수", "2102", Role.USER)).getId();
        projectMemberRepository.save(ProjectMember.builder()
                .project(project).userId(authorId).role(ClientRole.LEADER).part("BE").build());
    }

    private Long createPublic(String title, List<String> tags) {
        return service.create(authorId, new CreateRetrospectiveRequest(
                RetrospectiveCategory.RETROSPECTIVE, RetrospectiveVisibility.PUBLIC, title, "내용", null, tags));
    }

    @Test
    void 목록과_상세_응답에_태그와_팀_정보가_포함된다() {
        Long id = createPublic("회고", List.of("React", " 성능 개선 ", "React"));

        ListRetrospectiveResponse listed = service
                .getList(readerId, null, null, null, false, PageRequest.of(0, 10)).getContent().get(0);
        assertThat(listed.tags()).containsExactly("React", "성능 개선");
        assertThat(listed.projectId()).isEqualTo(projectId);
        assertThat(listed.teamName()).isEqualTo("더모먼트");

        DetailRetrospectiveResponse detail = service.getDetail(readerId, id);
        assertThat(detail.tags()).containsExactly("React", "성능 개선");
        assertThat(detail.projectId()).isEqualTo(projectId);
        assertThat(detail.teamName()).isEqualTo("더모먼트");
    }

    @Test
    void 수정_시_tags를_보내면_교체되고_생략하면_유지된다() {
        Long id = createPublic("회고", List.of("React"));

        service.update(authorId, id, new RetrospectivePatchRequest(null, null, "제목 변경", null, null, null));
        assertThat(service.getDetail(authorId, id).tags()).containsExactly("React");

        service.update(authorId, id, new RetrospectivePatchRequest(null, null, null, null, null, List.of("회고")));
        assertThat(service.getDetail(authorId, id).tags()).containsExactly("회고");
    }

    @Test
    void bookmarkedOnly는_내가_북마크한_회고만_반환한다() {
        Long bookmarked = createPublic("북마크한 글", List.of());
        createPublic("북마크 안 한 글", List.of());
        service.toggleBookmark(readerId, bookmarked);

        assertThat(service.getList(readerId, null, null, null, false, PageRequest.of(0, 10)).getTotalElements())
                .isEqualTo(2);
        assertThat(service.getList(readerId, null, null, null, true, PageRequest.of(0, 10)).getContent())
                .extracting(ListRetrospectiveResponse::id).containsExactly(bookmarked);
        assertThat(service.getList(authorId, null, null, null, true, PageRequest.of(0, 10)).getTotalElements())
                .isZero();
    }

    @Test
    void 임시저장_목록에_본문_태그_관련작업_팀_정보가_포함된다() {
        service.createDraft(authorId, new RetrospectivePatchRequest(
                null, null, "임시 제목", "임시 본문",
                List.of(new RelatedWorkRequest(RelatedWorkType.PR, 12, "로그인 개선")),
                List.of("초안")));

        List<DetailRetrospectiveResponse> drafts = service.getMyDrafts(authorId);

        assertThat(drafts).hasSize(1);
        DetailRetrospectiveResponse draft = drafts.get(0);
        assertThat(draft.content()).isEqualTo("임시 본문");
        assertThat(draft.tags()).containsExactly("초안");
        assertThat(draft.relatedWorks()).hasSize(1);
        assertThat(draft.projectId()).isEqualTo(projectId);
        assertThat(draft.teamName()).isEqualTo("더모먼트");
        assertThat(service.getMyDrafts(readerId)).isEmpty();
    }
}
