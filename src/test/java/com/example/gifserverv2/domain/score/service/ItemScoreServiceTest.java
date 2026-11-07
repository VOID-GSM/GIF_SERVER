package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.project.repository.ProjectRepository;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest;
import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest.ItemScore;
import com.example.gifserverv2.domain.score.dto.response.ProjectItemScoreResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreAreaResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreItemResponse;
import com.example.gifserverv2.domain.score.entity.ProjectItemScore;
import com.example.gifserverv2.domain.score.repository.ProjectItemScoreRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({ItemScoreService.class, ScoreCriteriaService.class, ScoreSupport.class})
class ItemScoreServiceTest {

    @Autowired ItemScoreService service;
    @Autowired ScoreCriteriaService criteriaService;
    @Autowired ProjectRepository projectRepository;
    @Autowired ProjectItemScoreRepository itemScoreRepository;
    @Autowired EntityManager entityManager;

    private final AuthenticatedUser master = teacher(1L, AdminRole.MASTER);
    private final AuthenticatedUser majorTeacher = teacher(2L, AdminRole.MAJOR_TEACHER);

    private Long projectId;
    private ScoreAreaResponse area;
    private ScoreItemResponse completeness;
    private ScoreItemResponse presentation;

    @BeforeEach
    void setUp() {
        projectId = projectRepository.save(Project.builder().name("GIF").teamName("더모먼트").build()).getId();
        area = criteriaService.createArea(master, new CreateScoreAreaRequest("전공", null));
        completeness = criteriaService.createItem(master, area.id(), new CreateScoreItemRequest("완성도", null, List.of(40, 32, 24), null));
        presentation = criteriaService.createItem(master, area.id(), new CreateScoreItemRequest("발표", null, List.of(20, 10), null));
        clearContext();
    }

    @Test
    void 선택지_점수로_채점하면_저장되고_다시_보내면_수정된다() {
        ProjectItemScoreResponse first = service.saveScores(projectId, majorTeacher, request(
                new ItemScore(completeness.id(), 32), new ItemScore(presentation.id(), 20)));
        clearContext();

        ProjectItemScoreResponse second = service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 40)));
        clearContext();

        assertThat(first.totalScore()).isEqualTo(52);
        assertThat(second.totalScore()).isEqualTo(60);
        assertThat(second.totalMaxScore()).isEqualTo(60);
        assertThat(second.areas().get(0).items())
                .extracting(ProjectItemScoreResponse.Item::name, ProjectItemScoreResponse.Item::score)
                .containsExactly(tuple("완성도", 40), tuple("발표", 20));
        assertThat(itemScoreRepository.count()).isEqualTo(2);
    }

    @Test
    void 채점하지_않은_항목은_null이고_선생님마다_따로_기록된다() {
        service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 24)));
        clearContext();

        ProjectItemScoreResponse mine = service.getScores(projectId, majorTeacher);
        ProjectItemScoreResponse others = service.getScores(projectId, master);

        assertThat(mine.areas().get(0).items()).extracting(ProjectItemScoreResponse.Item::score).containsExactly(24, null);
        assertThat(mine.areas().get(0).items().get(0).options()).containsExactly(40, 32, 24);
        assertThat(others.totalScore()).isZero();
        assertThat(others.areas().get(0).items()).extracting(ProjectItemScoreResponse.Item::score).containsOnlyNulls();
    }

    @Test
    void 선택지에_없는_점수는_거부되고_아무것도_저장되지_않는다() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.saveScores(projectId, majorTeacher, request(
                new ItemScore(presentation.id(), 20), new ItemScore(completeness.id(), 35))));

        assertThat(itemScoreRepository.count()).isZero();
    }

    @Test
    void 같은_항목을_두_번_보내거나_없는_대상이면_거부된다() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.saveScores(projectId, majorTeacher, request(
                new ItemScore(completeness.id(), 40), new ItemScore(completeness.id(), 32))));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.saveScores(projectId, majorTeacher, request(new ItemScore(9999L, 40))));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.saveScores(9999L, majorTeacher, request(new ItemScore(completeness.id(), 40))));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getScores(9999L, majorTeacher));
    }

    @Test
    void VOID_권한은_채점할_수_없다() {
        AuthenticatedUser voidUser = teacher(3L, AdminRole.VOID);

        assertStatus(HttpStatus.FORBIDDEN, () -> service.saveScores(projectId, voidUser, request(new ItemScore(completeness.id(), 40))));
        assertStatus(HttpStatus.FORBIDDEN, () -> service.getScores(projectId, voidUser));
        assertStatus(HttpStatus.FORBIDDEN, () -> service.saveScores(projectId, teacher(4L, null), request(new ItemScore(completeness.id(), 40))));
    }

    @Test
    void 채점된_항목을_삭제해도_점수_기록은_남고_더_이상_채점할_수_없다() {
        service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 40), new ItemScore(presentation.id(), 10)));
        clearContext();

        criteriaService.deleteItem(master, area.id(), completeness.id());
        clearContext();

        ProjectItemScoreResponse response = service.getScores(projectId, majorTeacher);
        assertThat(response.areas().get(0).items()).extracting(ProjectItemScoreResponse.Item::name).containsExactly("발표");
        assertThat(response.totalScore()).isEqualTo(10);
        assertThat(itemScoreRepository.count()).isEqualTo(2);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 32))));
    }

    @Test
    void 채점된_영역을_삭제해도_점수_기록은_남는다() {
        service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 40)));
        clearContext();

        criteriaService.deleteArea(master, area.id());
        clearContext();

        assertThat(service.getScores(projectId, majorTeacher).areas()).isEmpty();
        assertThat(itemScoreRepository.findAll()).extracting(ProjectItemScore::getScore).containsExactly(40);
    }

    @Test
    void 채점_후_선택지가_바뀌어도_기록된_점수는_유지된다() {
        service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 32)));
        clearContext();

        criteriaService.updateItem(master, area.id(), completeness.id(), new PatchScoreItemRequest(null, null, List.of(50, 25), null));
        clearContext();

        ProjectItemScoreResponse.Item item = service.getScores(projectId, majorTeacher).areas().get(0).items().get(0);
        assertThat(item.options()).containsExactly(50, 25);
        assertThat(item.maxScore()).isEqualTo(50);
        assertThat(item.score()).isEqualTo(32);
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.saveScores(projectId, majorTeacher, request(new ItemScore(completeness.id(), 32))));
    }

    private SaveItemScoresRequest request(ItemScore... scores) {
        return new SaveItemScoresRequest(List.of(scores));
    }

    private void clearContext() {
        entityManager.flush();
        entityManager.clear();
    }

    private void assertStatus(HttpStatus status, Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    private static AuthenticatedUser teacher(Long userId, AdminRole adminRole) {
        return new AuthenticatedUser(userId, "teacher@gsm.hs.kr", "선생님", null, Role.ADMIN, adminRole, null, false, null);
    }
}
