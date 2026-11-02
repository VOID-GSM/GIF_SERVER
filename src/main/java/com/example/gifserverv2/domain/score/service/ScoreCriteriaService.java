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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoreCriteriaService {

    private final ScoreAreaRepository areaRepository;
    private final ScoreItemRepository itemRepository;

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
        if (areaRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 영역 이름입니다.");
        }

        int displayOrder = request.displayOrder() != null ? request.displayOrder() : areaRepository.findNextDisplayOrder();
        return ScoreAreaResponse.from(areaRepository.save(new ScoreArea(name, displayOrder)));
    }

    @Transactional
    public ScoreAreaResponse updateArea(AuthenticatedUser user, Long areaId, PatchScoreAreaRequest request) {
        requireMaster(user);

        ScoreArea area = getAreaOrThrow(areaId);
        String name = request.name() != null ? request.name().trim() : null;
        if (name != null && areaRepository.existsByNameAndIdNot(name, areaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 존재하는 영역 이름입니다.");
        }

        area.update(name, request.displayOrder());
        return ScoreAreaResponse.from(area);
    }

    @Transactional
    public void deleteArea(AuthenticatedUser user, Long areaId) {
        requireMaster(user);
        areaRepository.delete(getAreaOrThrow(areaId));
    }

    @Transactional
    public ScoreItemResponse createItem(AuthenticatedUser user, Long areaId, CreateScoreItemRequest request) {
        requireMaster(user);

        ScoreArea area = getAreaOrThrow(areaId);
        String name = request.name().trim();
        if (itemRepository.existsByAreaIdAndName(areaId, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "영역에 이미 존재하는 항목 이름입니다.");
        }

        int displayOrder = request.displayOrder() != null ? request.displayOrder() : itemRepository.findNextDisplayOrder(areaId);
        ScoreItem item = ScoreItem.builder()
                .area(area)
                .name(name)
                .criteria(normalizeCriteria(request.criteria()))
                .maxScore(request.maxScore())
                .displayOrder(displayOrder)
                .build();
        return ScoreItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ScoreItemResponse updateItem(AuthenticatedUser user, Long areaId, Long itemId, PatchScoreItemRequest request) {
        requireMaster(user);

        ScoreItem item = getItemOrThrow(areaId, itemId);
        String name = request.name() != null ? request.name().trim() : null;
        if (name != null && itemRepository.existsByAreaIdAndNameAndIdNot(areaId, name, itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "영역에 이미 존재하는 항목 이름입니다.");
        }

        item.update(name, request.maxScore(), request.displayOrder());
        if (request.criteria() != null) {
            item.updateCriteria(normalizeCriteria(request.criteria()));
        }
        return ScoreItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(AuthenticatedUser user, Long areaId, Long itemId) {
        requireMaster(user);
        itemRepository.delete(getItemOrThrow(areaId, itemId));
    }

    private ScoreArea getAreaOrThrow(Long areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "점수 영역을 찾을 수 없습니다."));
    }

    private ScoreItem getItemOrThrow(Long areaId, Long itemId) {
        return itemRepository.findByIdAndAreaId(itemId, areaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "평가 항목을 찾을 수 없습니다."));
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
