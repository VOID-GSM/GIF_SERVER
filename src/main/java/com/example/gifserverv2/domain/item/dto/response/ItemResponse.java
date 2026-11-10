package com.example.gifserverv2.domain.item.dto.response;

import com.example.gifserverv2.domain.item.entity.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이템")
public record ItemResponse(
        @Schema(description = "아이템 ID", example = "7")
        Long id,
        @Schema(description = "이름", example = "빨간 모자")
        String name,
        @Schema(description = "프론트에서 리소스를 찾는 키", example = "hair_red_cap")
        String itemKey,
        @Schema(description = "카테고리. EXPRESSION / HAIR / CLOTH / OBJECT / DECORATION / GLASSES", example = "HAIR")
        ItemCategory category,
        @Schema(description = "가격 (코인)", example = "50")
        int price,
        @Schema(description = "기본 지급 아이템 여부", example = "false")
        boolean isDefault,
        @Schema(description = "이미지 URL")
        String imageUrl,
        @Schema(description = "보유 여부", example = "true")
        boolean isOwned,
        @Schema(description = "장착 여부", example = "false")
        boolean isEquipped
) {}