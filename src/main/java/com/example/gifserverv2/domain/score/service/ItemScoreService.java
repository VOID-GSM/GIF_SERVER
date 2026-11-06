package com.example.gifserverv2.domain.score.service;

import com.example.gifserverv2.domain.project.entity.Project;
import com.example.gifserverv2.domain.score.dto.request.SaveItemScoresRequest;
import com.example.gifserverv2.domain.score.dto.response.ProjectItemScoreResponse;
import com.example.gifserverv2.domain.score.entity.ProjectItemScore;
import com.example.gifserverv2.domain.score.entity.ScoreArea;
import com.example.gifserverv2.domain.score.entity.ScoreItem;
import com.example.gifserverv2.domain.score.repository.ProjectItemScoreRepository;
import com.example.gifserverv2.domain.score.repository.ScoreAreaRepository;
import com.example.gifserverv2.domain.score.repository.ScoreItemRepository;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 평가 항목(score_item) 기반 채점. 기존 major/report/social 채점(Score)과는 별개로 동작한다.
 */
@Service
@RequiredArgsConstructor
public class ItemScoreService {

    private final ScoreSupport support;
    private final ScoreAreaRepository areaRepository;
    private final ScoreItemRepository itemRepository;
    private final ProjectItemScoreRepository itemScoreRepository;

    @Transactional(readOnly = true)
    public ProjectItemScoreResponse getScores(Long projectId, AuthenticatedUser evaluator) {
        validateEvaluator(evaluator);
        support.getProjectOrThrow(projectId);
        return buildResponse(projectId, evaluator.userId());
    }

    @Transactional
    public ProjectItemScoreResponse saveScores(Long projectId, AuthenticatedUser evaluator, SaveItemScoresRequest request) {
        validateEvaluator(evaluator);
        Project project = support.getProjectOrThrow(projectId);
        Long evaluatorId = evaluator.userId();

        List<Long> itemIds = request.scores().stream().map(SaveItemScoresRequest.ItemScore::itemId).toList();
        Set<Long> uniqueItemIds = Set.copyOf(itemIds);
        if (uniqueItemIds.size() != itemIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 항목이 여러 번 포함되었습니다.");
        }

        Map<Long, ScoreItem> items = itemRepository.findByIdInAndDeletedAtIsNull(uniqueItemIds).stream()
                .collect(Collectors.toMap(ScoreItem::getId, Function.identity()));
        if (items.size() != uniqueItemIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "평가 항목을 찾을 수 없습니다.");
        }

        for (SaveItemScoresRequest.ItemScore entry : request.scores()) {
            ScoreItem item = items.get(entry.itemId());
            if (!item.isSelectable(entry.score())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "'" + item.getName() + "' 항목의 선택지에 없는 점수입니다. (선택지: " + item.getOptions() + ")");
            }
        }

        Map<Long, ProjectItemScore> existing = itemScoreRepository.findByProjectIdAndEvaluatorId(projectId, evaluatorId).stream()
                .collect(Collectors.toMap(s -> s.getItem().getId(), Function.identity()));

        // 같은 선생님이 같은 항목을 동시에 처음 채점하면 유니크 제약(uk_project_item_score)에서 막힌다.
        // IDENTITY 전략이라 INSERT 는 save 시점에 바로 실행되므로 save 까지 try 안에 둔다.
        try {
            for (SaveItemScoresRequest.ItemScore entry : request.scores()) {
                ProjectItemScore score = existing.get(entry.itemId());
                if (score != null) {
                    score.changeScore(entry.score());
                } else {
                    itemScoreRepository.save(new ProjectItemScore(project, evaluatorId, items.get(entry.itemId()), entry.score()));
                }
            }
            itemScoreRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "동시에 채점 요청이 들어왔습니다. 다시 시도해 주세요.", e);
        }

        return buildResponse(projectId, evaluatorId);
    }

    private ProjectItemScoreResponse buildResponse(Long projectId, Long evaluatorId) {
        Map<Long, Integer> scoreByItemId = itemScoreRepository.findByProjectIdAndEvaluatorId(projectId, evaluatorId).stream()
                .collect(Collectors.toMap(s -> s.getItem().getId(), ProjectItemScore::getScore));

        List<ProjectItemScoreResponse.Area> areas = areaRepository.findAllWithItems().stream()
                .map(area -> toArea(area, scoreByItemId))
                .toList();

        return new ProjectItemScoreResponse(
                projectId,
                areas.stream().mapToInt(ProjectItemScoreResponse.Area::totalScore).sum(),
                areas.stream().mapToInt(ProjectItemScoreResponse.Area::totalMaxScore).sum(),
                areas);
    }

    private ProjectItemScoreResponse.Area toArea(ScoreArea area, Map<Long, Integer> scoreByItemId) {
        List<ProjectItemScoreResponse.Item> items = area.getItems().stream()
                .sorted(Comparator.comparingInt(ScoreItem::getDisplayOrder).thenComparing(ScoreItem::getId))
                .map(item -> new ProjectItemScoreResponse.Item(
                        item.getId(),
                        item.getName(),
                        item.getCriteria(),
                        item.getMaxScore(),
                        List.copyOf(item.getOptions()),
                        scoreByItemId.get(item.getId())))
                .toList();

        return new ProjectItemScoreResponse.Area(
                area.getId(),
                area.getName(),
                items.stream().mapToInt(i -> i.score() != null ? i.score() : 0).sum(),
                items.stream().mapToInt(ProjectItemScoreResponse.Item::maxScore).sum(),
                items);
    }

    private void validateEvaluator(AuthenticatedUser evaluator) {
        support.validateEvaluator(evaluator, e -> e.adminRole() != null, "점수를 부여할 권한이 없습니다.");
    }
}
