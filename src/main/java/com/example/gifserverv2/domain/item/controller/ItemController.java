package com.example.gifserverv2.domain.item.controller;

import com.example.gifserverv2.domain.item.dto.request.BuyItemRequest;
import com.example.gifserverv2.domain.item.dto.request.EquipItemsRequest;
import com.example.gifserverv2.domain.item.dto.response.ItemResponse;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.service.ItemService;
import com.example.gifserverv2.global.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public List<ItemResponse> getAllItems(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        validateUser(currentUser);
        return itemService.getAllItems(currentUser.userId());
    }

    @GetMapping("/category/{category}")
    public List<ItemResponse> getItemsByCategory(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable ItemCategory category
    ) {
        validateUser(currentUser);
        return itemService.getItemsByCategory(currentUser.userId(), category);
    }

    @PostMapping("/buy")
    public ItemResponse buyItem(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody BuyItemRequest request
    ) {
        validateUser(currentUser);
        return itemService.buyItem(currentUser.userId(), request);
    }

    @PostMapping("/equip")
    public List<ItemResponse> equipItems(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody EquipItemsRequest request
    ) {
        validateUser(currentUser);
        return itemService.equipItems(currentUser.userId(), request);
    }

    private void validateUser(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.userId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }
    }
}