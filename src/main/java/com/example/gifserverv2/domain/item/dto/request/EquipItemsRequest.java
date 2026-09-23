package com.example.gifserverv2.domain.item.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EquipItemsRequest(
        @NotNull(message = "착용할 아이템 목록은 필수입니다.")
        List<Long> itemIds
) {}