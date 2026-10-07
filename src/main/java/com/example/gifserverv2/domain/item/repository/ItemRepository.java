package com.example.gifserverv2.domain.item.repository;

import com.example.gifserverv2.domain.item.entity.Item;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByCategory(ItemCategory category);

    Optional<Item> findByItemKey(String itemKey);

    List<Item> findByIsDefaultTrue();
}