package com.example.gifserverv2.domain.item.dto.response;

import com.example.gifserverv2.domain.item.entity.Item;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.entity.UserItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MyItemResponse(
        @Schema(description = "아이템 ID", example = "3") Long itemId,
        @Schema(description = "아이템 이름") String name,
        @Schema(description = "카테고리") ItemCategory category,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "가격(코인)", example = "100") int price,
        @Schema(description = "장착 여부") boolean equipped,
        @Schema(description = "구매 시각. 이 기능 추가 전에 산 아이템은 null", nullable = true) LocalDateTime purchasedAt
) {
    public static MyItemResponse from(UserItem userItem) {
        Item item = userItem.getItem();
        return new MyItemResponse(item.getId(), item.getName(), item.getCategory(), item.getImageUrl(),
                item.getPrice(), userItem.isEquipped(), userItem.getCreatedAt());
    }
}