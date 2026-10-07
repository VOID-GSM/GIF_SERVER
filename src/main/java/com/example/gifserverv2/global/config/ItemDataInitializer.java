package com.example.gifserverv2.global.config;

import com.example.gifserverv2.domain.item.entity.Item;
import com.example.gifserverv2.domain.item.entity.ItemType;
import com.example.gifserverv2.domain.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ItemDataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (ItemType type : ItemType.values()) {
            itemRepository.findByItemKey(type.getItemKey())
                    .orElseGet(() -> itemRepository.save(Item.builder()
                            .itemKey(type.getItemKey())
                            .name(type.getName())
                            .category(type.getCategory())
                            .price(type.getPrice())
                            .isDefault(type.isDefault())
                            .imageUrl("/images/items/" + type.getItemKey() + ".png")
                            .build()));
        }
    }
}