package com.example.gifserverv2.domain.item.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "아이템 구매 요청")
public record BuyItemRequest(
        @Schema(description = "아이템 ID", example = "7")
        @NotNull(message = "아이템 ID는 필수입니다.")
        Long itemId
) {}