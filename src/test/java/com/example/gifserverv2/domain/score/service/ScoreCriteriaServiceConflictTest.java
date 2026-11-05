package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * 사전 중복 검사를 동시에 통과한 뒤 DB 유니크 제약에서 막히는 경우를 재현한다.
 */
@ExtendWith(MockitoExtension.class)
class ScoreCriteriaServiceConflictTest {

    @Mock ScoreAreaRepository areaRepository;
    @Mock ScoreItemRepository itemRepository;
    @InjectMocks ScoreCriteriaService service;

    private final AuthenticatedUser master =
            new AuthenticatedUser(1L, "teacher@gsm.hs.kr", "선생님", null, Role.ADMIN, AdminRole.MASTER, null, false, null);
    private final DataIntegrityViolationException uniqueViolation =
            new DataIntegrityViolationException("uk violation");

    @Test
    void 영역_생성_중_유니크_제약에_걸리면_409() {
        given(areaRepository.existsByName("전공")).willReturn(false);
        given(areaRepository.saveAndFlush(any(ScoreArea.class))).willThrow(uniqueViolation);

        assertConflict(() -> service.createArea(master, new CreateScoreAreaRequest("전공", 0)));
    }

    @Test
    void 영역_수정_중_유니크_제약에_걸리면_409() {
        given(areaRepository.findById(1L)).willReturn(Optional.of(new ScoreArea("사회", 0)));
        given(areaRepository.existsByNameAndIdNot("전공", 1L)).willReturn(false);
        given(areaRepository.saveAndFlush(any(ScoreArea.class))).willThrow(uniqueViolation);

        assertConflict(() -> service.updateArea(master, 1L, new PatchScoreAreaRequest("전공", null)));
    }

    @Test
    void 항목_생성_중_유니크_제약에_걸리면_409() {
        given(areaRepository.findById(1L)).willReturn(Optional.of(new ScoreArea("전공", 0)));
        given(itemRepository.existsByAreaIdAndName(1L, "발표")).willReturn(false);
        given(itemRepository.saveAndFlush(any(ScoreItem.class))).willThrow(uniqueViolation);

        assertConflict(() -> service.createItem(master, 1L, new CreateScoreItemRequest("발표", null, 20, 0)));
    }

    @Test
    void 항목_수정_중_유니크_제약에_걸리면_409() {
        ScoreItem item = ScoreItem.builder()
                .area(new ScoreArea("전공", 0))
                .name("완성도")
                .maxScore(40)
                .displayOrder(0)
                .build();
        given(itemRepository.findByIdAndAreaId(2L, 1L)).willReturn(Optional.of(item));
        given(itemRepository.existsByAreaIdAndNameAndIdNot(eq(1L), anyString(), anyLong())).willReturn(false);
        given(itemRepository.saveAndFlush(any(ScoreItem.class))).willThrow(uniqueViolation);

        assertConflict(() -> service.updateItem(master, 1L, 2L, new PatchScoreItemRequest("발표", null, null, null)));
    }

    private void assertConflict(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
