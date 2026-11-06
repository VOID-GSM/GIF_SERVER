package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest;
import com.example.gifserverv2.domain.score.entity.ProjectItemScore;
import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;
import com.example.gifserverv2.domain.score.repository.ProjectItemScoreRepository;
import com.example.gifserverv2.domain.score.repository.ScoreAreaRepository;
import com.example.gifserverv2.domain.score.repository.ScoreItemRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;

/**
 * 같은 항목을 동시에 처음 채점해서, 신규 INSERT(save) 시점에 유니크 제약에서 막히는 경우를 재현한다.
 */
@ExtendWith(MockitoExtension.class)
class ItemScoreServiceConflictTest {

    @Mock ScoreSupport support;
    @Mock ScoreAreaRepository areaRepository;
    @Mock ScoreItemRepository itemRepository;
    @Mock ProjectItemScoreRepository itemScoreRepository;
    @InjectMocks ItemScoreService service;

    private final AuthenticatedUser teacher =
            new AuthenticatedUser(1L, "teacher@gsm.hs.kr", "선생님", null, Role.ADMIN, AdminRole.MAJOR_TEACHER, null, false, null);

    @Test
    void 신규_채점_저장_중_유니크_제약에_걸리면_409() {
        ScoreItem item = ScoreItem.builder()
                .area(new ScoreArea("전공", 0))
                .name("완성도")
                .options(List.of(40, 32))
                .displayOrder(0)
                .build();
        ReflectionTestUtils.setField(item, "id", 10L);

        given(support.getProjectOrThrow(1L)).willReturn(Project.builder().id(1L).name("GIF").teamName("더모먼트").build());
        given(itemRepository.findByIdInAndDeletedAtIsNull(anyCollection())).willReturn(List.of(item));
        given(itemScoreRepository.findByProjectIdAndEvaluatorId(1L, 1L)).willReturn(List.of());
        given(itemScoreRepository.save(any(ProjectItemScore.class)))
                .willThrow(new DataIntegrityViolationException("uk_project_item_score"));

        SaveItemScoresRequest request = new SaveItemScoresRequest(List.of(new SaveItemScoresRequest.ItemScore(10L, 40)));

        assertThatThrownBy(() -> service.saveScores(1L, teacher, request))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
