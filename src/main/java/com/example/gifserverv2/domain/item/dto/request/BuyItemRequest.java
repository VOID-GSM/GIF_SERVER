package com.example.gifserverv2.domain.item.dto.request;

import jakarta.validation.constraints.NotNull;

public record BuyItemRequest(
        @NotNull(message = "아이템 ID는 필수입니다.")
        Long itemId
) {}