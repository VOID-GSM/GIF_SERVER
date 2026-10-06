package com.example.gifserverv2.domain.item.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.item.dto.request.BuyItemRequest;
import com.example.gifserverv2.domain.item.dto.request.EquipItemsRequest;
import com.example.gifserverv2.domain.item.dto.response.ItemResponse;
import com.example.gifserverv2.domain.item.entity.Item;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.entity.UserItem;
import com.example.gifserverv2.domain.item.repository.ItemRepository;
import com.example.gifserverv2.domain.item.repository.UserItemRepository;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final CoinAccountRepository coinAccountRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(Long userId) {
        List<Item> items = itemRepository.findAll();
        return mapToItemResponses(userId, items);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByCategory(Long userId, ItemCategory category) {
        List<Item> items = itemRepository.findByCategory(category);
        return mapToItemResponses(userId, items);
    }

    @Transactional
    public ItemResponse buyItem(Long userId, BuyItemRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 아이템입니다."));

        if (userItemRepository.existsByUserIdAndItemId(userId, item.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 보유한 아이템입니다.");
        }

        CoinAccount coinAccount = coinAccountRepository.findByUserId(userId)
                .orElseGet(() -> coinAccountRepository.save(CoinAccount.open(userId)));

        if (coinAccount.getBalance() < item.getPrice()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "코인이 부족합니다.");
        }

        coinAccount.deductCoin(item.getPrice());
        user.updateCoinBalance(coinAccount.getBalance());

        UserItem userItem = UserItem.builder()
                .user(user)
                .item(item)
                .isEquipped(false)
                .build();
        userItemRepository.save(userItem);

        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getItemKey(),
                item.getCategory(),
                item.getPrice(),
                item.isDefault(),
                item.getImageUrl(),
                true,
                false
        );
    }

    @Transactional
    public List<ItemResponse> equipItems(Long userId, EquipItemsRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        List<UserItem> userItems = userItemRepository.findAllByUserIdWithItem(userId);
        Map<Long, UserItem> userItemMap = userItems.stream()
                .collect(Collectors.toMap(ui -> ui.getItem().getId(), ui -> ui));

        Set<Long> targetEquipIds = new HashSet<>(request.itemIds());

        for (Long itemId : targetEquipIds) {
            if (!userItemMap.containsKey(itemId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "보유하지 않은 아이템은 장착할 수 없습니다.");
            }
        }

        Map<ItemCategory, Long> categoryEquipCount = new HashMap<>();
        for (Long itemId : targetEquipIds) {
            ItemCategory category = userItemMap.get(itemId).getItem().getCategory();
            if (categoryEquipCount.containsKey(category)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카테고리당 하나의 아이템만 장착할 수 있습니다.");
            }
            categoryEquipCount.put(category, itemId);
        }

        for (UserItem userItem : userItems) {
            if (targetEquipIds.contains(userItem.getItem().getId())) {
                userItem.equip();
            } else {
                userItem.unequip();
            }
        }

        return getAllItems(userId);
    }

    private List<ItemResponse> mapToItemResponses(Long userId, List<Item> items) {
        List<UserItem> userItems = userItemRepository.findAllByUserIdWithItem(userId);

        Map<Long, UserItem> userItemMap = userItems.stream()
                .collect(Collectors.toMap(ui -> ui.getItem().getId(), ui -> ui));

        return items.stream().map(item -> {
            UserItem userItem = userItemMap.get(item.getId());
            boolean isOwned = userItem != null;
            boolean isEquipped = isOwned && userItem.isEquipped();

            return new ItemResponse(
                    item.getId(),
                    item.getName(),
                    item.getItemKey(),
                    item.getCategory(),
                    item.getPrice(),
                    item.isDefault(),
                    item.getImageUrl(),
                    isOwned,
                    isEquipped
            );
        }).collect(Collectors.toList());
    }
}