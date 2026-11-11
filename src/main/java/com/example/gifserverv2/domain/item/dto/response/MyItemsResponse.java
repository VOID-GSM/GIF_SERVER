package com.example.gifserverv2.domain.item.dto.response;

import com.example.gifserverv2.domain.item.entity.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record MyItemsResponse(
        @Schema(description = "보유 아이템 (최신 구매순)") List<MyItemResponse> items,
        @Schema(description = "현재 장착 중인 아이템 (카테고리 → 아이템). 장착한 카테고리만 들어 있다") Map<ItemCategory, MyItemResponse> equipped
) {
}