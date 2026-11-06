package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.score.dto.request.CreateScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.CreateScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreAreaRequest;
import com.example.gifserverv2.domain.score.dto.request.PatchScoreItemRequest;
import com.example.gifserverv2.domain.score.dto.response.ScoreAreaResponse;
import com.example.gifserverv2.domain.score.dto.response.ScoreItemResponse;
import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;
import com.example.gifserverv2.domain.score.repository.ScoreAreaRepository;
import com.example.gifserverv2.domain.score.repository.ScoreItemRepository;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class ScoreCriteriaService {

    private final ScoreAreaRepository areaRepository;
    private final ScoreItemRepository itemRepository;

    private static final String DUPLICATE_AREA_NAME = "이미 존재하는 영역 이름입니다.";
    private static final String DUPLICATE_ITEM_NAME = "영역에 이미 존재하는 항목 이름입니다.";

    @Transactional(readOnly = true)
    public List<ScoreAreaResponse> getAreas() {
        return areaRepository.findAllWithItems().stream()
                .map(ScoreAreaResponse::from)
                .toList();
    }

    @Transactional
    public ScoreAreaResponse createArea(AuthenticatedUser user, CreateScoreAreaRequest request) {
        requireMaster(user);

        String name = request.name().trim();
        if (areaRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_AREA_NAME);
        }

        int displayOrder = request.displayOrder() != null ? request.displayOrder() : areaRepository.findNextDisplayOrder();
        ScoreArea saved = saveOrConflict(() -> areaRepository.saveAndFlush(new ScoreArea(name, displayOrder)), DUPLICATE_AREA_NAME);
        return ScoreAreaResponse.from(saved);
    }

    @Transactional
    public ScoreAreaResponse updateArea(AuthenticatedUser user, Long areaId, PatchScoreAreaRequest request) {
        requireMaster(user);

        ScoreArea area = getAreaOrThrow(areaId);
        String name = request.name() != null ? request.name().trim() : null;
        if (name != null && areaRepository.existsByNameAndDeletedAtIsNullAndIdNot(name, areaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_AREA_NAME);
        }

        area.update(name, request.displayOrder());
        saveOrConflict(() -> areaRepository.saveAndFlush(area), DUPLICATE_AREA_NAME);
        return ScoreAreaResponse.from(area);
    }

    @Transactional
    public void deleteArea(AuthenticatedUser user, Long areaId) {
        requireMaster(user);
        // 행을 지우지 않는다. 하위 항목도 함께 삭제 처리되고, 기존 채점 기록은 그대로 남는다.
        getAreaOrThrow(areaId).softDelete();
    }

    @Transactional
    public ScoreItemResponse createItem(AuthenticatedUser user, Long areaId, CreateScoreItemRequest request) {
        requireMaster(user);

        ScoreArea area = getAreaOrThrow(areaId);
        String name = request.name().trim();
        if (itemRepository.existsByAreaIdAndNameAndDeletedAtIsNull(areaId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_ITEM_NAME);
        }

        int displayOrder = request.displayOrder() != null ? request.displayOrder() : itemRepository.findNextDisplayOrder(areaId);
        ScoreItem item = ScoreItem.builder()
                .area(area)
                .name(name)
                .criteria(normalizeCriteria(request.criteria()))
                .options(validateOptions(request.options()))
                .displayOrder(displayOrder)
                .build();
        return ScoreItemResponse.from(saveOrConflict(() -> itemRepository.saveAndFlush(item), DUPLICATE_ITEM_NAME));
    }

    @Transactional
    public ScoreItemResponse updateItem(AuthenticatedUser user, Long areaId, Long itemId, PatchScoreItemRequest request) {
        requireMaster(user);

        ScoreItem item = getItemOrThrow(areaId, itemId);
        String name = request.name() != null ? request.name().trim() : null;
        if (name != null && itemRepository.existsByAreaIdAndNameAndDeletedAtIsNullAndIdNot(areaId, name, itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, DUPLICATE_ITEM_NAME);
        }

        // 선택지가 바뀌어도 이미 기록된 점수는 바꾸지 않는다.
        List<Integer> options = request.options() != null ? validateOptions(request.options()) : null;
        item.update(name, options, request.displayOrder());
        if (request.criteria() != null) {
            item.updateCriteria(normalizeCriteria(request.criteria()));
        }
        saveOrConflict(() -> itemRepository.saveAndFlush(item), DUPLICATE_ITEM_NAME);
        return ScoreItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(AuthenticatedUser user, Long areaId, Long itemId) {
        requireMaster(user);
        getItemOrThrow(areaId, itemId).softDelete();
    }

    private ScoreArea getAreaOrThrow(Long areaId) {
        return areaRepository.findByIdAndDeletedAtIsNull(areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "점수 영역을 찾을 수 없습니다."));
    }

    private ScoreItem getItemOrThrow(Long areaId, Long itemId) {
        return itemRepository.findByIdAndAreaIdAndDeletedAtIsNull(itemId, areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "평가 항목을 찾을 수 없습니다."));
    }

    /**
     * 사전 중복 검사를 동시에 통과한 요청은 DB 유니크 제약에서 막히므로, 이 경우도 409로 응답한다.
     */
    private <T> T saveOrConflict(Supplier<T> save, String conflictMessage) {
        try {
            return save.get();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, conflictMessage, e);
        }
    }

    private List<Integer> validateOptions(List<Integer> options) {
        if (options.stream().distinct().count() != options.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택지 점수가 중복되었습니다.");
        }
        if (options.stream().noneMatch(score -> score > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택지 중 하나는 1점 이상이어야 합니다.");
        }
        return options;
    }

    private String normalizeCriteria(String criteria) {
        return criteria == null || criteria.isBlank() ? null : criteria.trim();
    }

    private void requireMaster(AuthenticatedUser user) {
        if (user == null || user.adminRole() != AdminRole.MASTER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "점수 영역 관리 권한이 없습니다. (Master 선생님 전용)");
        }
    }
}
