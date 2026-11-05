package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.response.ScoreAreaResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreItemResponse;
import com.example.gifserverv2.domain.score.repository.ScoreAreaRepository;
import com.example.gifserverv2.domain.score.repository.ScoreItemRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(ScoreCriteriaService.class)
class ScoreCriteriaServiceTest {

    @Autowired ScoreCriteriaService service;
    @Autowired ScoreAreaRepository areaRepository;
    @Autowired ScoreItemRepository itemRepository;
    @Autowired EntityManager entityManager;

    private final AuthenticatedUser master = admin(AdminRole.MASTER);

    @Test
    void 영역과_항목을_표시_순서대로_조회한다() {
        ScoreAreaResponse second = service.createArea(master, new CreateScoreAreaRequest("사회 중심 영역", 5));
        ScoreAreaResponse first = service.createArea(master, new CreateScoreAreaRequest("전공 중심 영역", 1));
        service.createItem(master, first.id(), new CreateScoreItemRequest("발표", null, 20, 2));
        service.createItem(master, first.id(), new CreateScoreItemRequest("완성도", "동작 여부 기준", 40, 1));
        clearContext();

        List<ScoreAreaResponse> areas = service.getAreas();

        assertThat(areas).extracting(ScoreAreaResponse::id).containsExactly(first.id(), second.id());
        assertThat(areas.get(0).totalMaxScore()).isEqualTo(60);
        assertThat(areas.get(0).items()).extracting(ScoreItemResponse::name).containsExactly("완성도", "발표");
        assertThat(areas.get(0).items().get(0).criteria()).isEqualTo("동작 여부 기준");
        assertThat(areas.get(1).items()).isEmpty();
    }

    @Test
    void 표시_순서를_생략하면_마지막_다음_순서가_된다() {
        ScoreAreaResponse first = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreAreaResponse second = service.createArea(master, new CreateScoreAreaRequest("사회", null));
        ScoreItemResponse itemFirst = service.createItem(master, first.id(), new CreateScoreItemRequest("발표", null, 20, null));
        ScoreItemResponse itemSecond = service.createItem(master, first.id(), new CreateScoreItemRequest("완성도", null, 40, null));

        assertThat(first.displayOrder()).isZero();
        assertThat(second.displayOrder()).isEqualTo(1);
        assertThat(itemFirst.displayOrder()).isZero();
        assertThat(itemSecond.displayOrder()).isEqualTo(1);
    }

    @Test
    void 이름은_앞뒤_공백을_제거해_저장하고_중복이면_409() {
        service.createArea(master, new CreateScoreAreaRequest("  전공  ", null));

        assertThat(areaRepository.existsByName("전공")).isTrue();
        assertStatus(HttpStatus.CONFLICT, () -> service.createArea(master, new CreateScoreAreaRequest("전공", null)));
    }

    @Test
    void 영역을_수정하고_다른_영역과_이름이_겹치면_409() {
        ScoreAreaResponse major = service.createArea(master, new CreateScoreAreaRequest("전공", 0));
        service.createArea(master, new CreateScoreAreaRequest("사회", 1));

        ScoreAreaResponse renamed = service.updateArea(master, major.id(), new PatchScoreAreaRequest("전공 중심", null));
        ScoreAreaResponse sameName = service.updateArea(master, major.id(), new PatchScoreAreaRequest("전공 중심", 3));

        assertThat(renamed.name()).isEqualTo("전공 중심");
        assertThat(sameName.displayOrder()).isEqualTo(3);
        assertStatus(HttpStatus.CONFLICT, () -> service.updateArea(master, major.id(), new PatchScoreAreaRequest("사회", null)));
    }

    @Test
    void 영역을_삭제하면_항목도_함께_삭제된다() {
        ScoreAreaResponse area = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        service.createItem(master, area.id(), new CreateScoreItemRequest("발표", null, 20, null));
        clearContext();

        service.deleteArea(master, area.id());
        clearContext();

        assertThat(areaRepository.findById(area.id())).isEmpty();
        assertThat(itemRepository.count()).isZero();
    }

    @Test
    void 항목의_기준과_배점을_수정한다() {
        ScoreAreaResponse area = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreItemResponse item = service.createItem(master, area.id(), new CreateScoreItemRequest("발표", "기존 기준", 20, null));

        ScoreItemResponse changed = service.updateItem(master, area.id(), item.id(), new PatchScoreItemRequest(null, "새 기준", 30, null));
        ScoreItemResponse kept = service.updateItem(master, area.id(), item.id(), new PatchScoreItemRequest("프레젠테이션", null, null, null));
        ScoreItemResponse cleared = service.updateItem(master, area.id(), item.id(), new PatchScoreItemRequest(null, " ", null, null));

        assertThat(changed.criteria()).isEqualTo("새 기준");
        assertThat(changed.maxScore()).isEqualTo(30);
        assertThat(kept.name()).isEqualTo("프레젠테이션");
        assertThat(kept.criteria()).isEqualTo("새 기준");
        assertThat(cleared.criteria()).isNull();
    }

    @Test
    void 같은_이름의_항목은_같은_영역에서만_막는다() {
        ScoreAreaResponse major = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreAreaResponse social = service.createArea(master, new CreateScoreAreaRequest("사회", null));
        service.createItem(master, major.id(), new CreateScoreItemRequest("발표", null, 20, null));

        ScoreItemResponse other = service.createItem(master, social.id(), new CreateScoreItemRequest("발표", null, 10, null));

        assertThat(other.areaId()).isEqualTo(social.id());
        assertStatus(HttpStatus.CONFLICT,
                () -> service.createItem(master, major.id(), new CreateScoreItemRequest("발표", null, 20, null)));
    }

    @Test
    void 항목을_삭제하면_목록과_총점에서_빠진다() {
        ScoreAreaResponse area = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreItemResponse removed = service.createItem(master, area.id(), new CreateScoreItemRequest("발표", null, 20, null));
        service.createItem(master, area.id(), new CreateScoreItemRequest("완성도", null, 40, null));
        clearContext();

        service.deleteItem(master, area.id(), removed.id());
        clearContext();

        ScoreAreaResponse result = service.getAreas().get(0);
        assertThat(result.items()).extracting(ScoreItemResponse::name).containsExactly("완성도");
        assertThat(result.totalMaxScore()).isEqualTo(40);
    }

    @Test
    void 다른_영역의_항목이나_없는_대상은_404() {
        ScoreAreaResponse major = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreAreaResponse social = service.createArea(master, new CreateScoreAreaRequest("사회", null));
        ScoreItemResponse item = service.createItem(master, major.id(), new CreateScoreItemRequest("발표", null, 20, null));

        assertStatus(HttpStatus.NOT_FOUND, () -> service.deleteItem(master, social.id(), item.id()));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.deleteArea(master, 9999L));
        assertStatus(HttpStatus.NOT_FOUND,
                () -> service.createItem(master, 9999L, new CreateScoreItemRequest("발표", null, 20, null)));
    }

    @Test
    void Master가_아니면_변경할_수_없고_데이터가_유지된다() {
        ScoreAreaResponse area = service.createArea(master, new CreateScoreAreaRequest("전공", null));
        ScoreItemResponse item = service.createItem(master, area.id(), new CreateScoreItemRequest("발표", null, 20, null));

        for (AuthenticatedUser user : List.of(admin(AdminRole.MAJOR_TEACHER), admin(AdminRole.GENERAL_TEACHER), admin(AdminRole.VOID), admin(null))) {
            assertStatus(HttpStatus.FORBIDDEN, () -> service.createArea(user, new CreateScoreAreaRequest("새 영역", null)));
            assertStatus(HttpStatus.FORBIDDEN, () -> service.updateArea(user, area.id(), new PatchScoreAreaRequest("변경", null)));
            assertStatus(HttpStatus.FORBIDDEN, () -> service.deleteArea(user, area.id()));
            assertStatus(HttpStatus.FORBIDDEN, () -> service.createItem(user, area.id(), new CreateScoreItemRequest("새 항목", null, 10, null)));
            assertStatus(HttpStatus.FORBIDDEN, () -> service.updateItem(user, area.id(), item.id(), new PatchScoreItemRequest(null, null, 99, null)));
            assertStatus(HttpStatus.FORBIDDEN, () -> service.deleteItem(user, area.id(), item.id()));
        }

        assertThat(areaRepository.count()).isEqualTo(1);
        assertThat(itemRepository.findById(item.id()).orElseThrow().getMaxScore()).isEqualTo(20);
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

    private static AuthenticatedUser admin(AdminRole adminRole) {
        return new AuthenticatedUser(1L, "teacher@gsm.hs.kr", "선생님", null, Role.ADMIN, adminRole, null, false, null);
    }
}
