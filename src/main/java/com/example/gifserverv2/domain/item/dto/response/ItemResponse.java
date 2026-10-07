package com.example.gifserverv2.domain.item.dto.response;

import com.example.gifserverv2.domain.item.entity.ItemCategory;

public record ItemResponse(
        Long id,
        String name,
        String itemKey,
        ItemCategory category,
        int price,
        boolean isDefault,
        String imageUrl,
        boolean isOwned,
        boolean isEquipped
) {}