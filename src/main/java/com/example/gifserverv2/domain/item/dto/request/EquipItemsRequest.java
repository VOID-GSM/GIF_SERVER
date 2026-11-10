package com.example.gifserverv2.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "아이템 장착 요청")
public record EquipItemsRequest(
        @Schema(description = "장착할 아이템 ID 전체 목록. 카테고리당 하나. 빈 배열이면 모두 해제", example = "[2, 7]")
        @NotNull(message = "착용할 아이템 목록은 필수입니다.")
        List<Long> itemIds
) {}