package com.example.gifserverv2.domain.item.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCategory category;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "item_key", nullable = false, length = 50, unique = true)
    private String itemKey;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    public Item(ItemCategory category, String name, String itemKey, int price, boolean isDefault, String imageUrl) {
        this.category = category;
        this.name = name;
        this.itemKey = itemKey;
        this.price = price;
        this.isDefault = isDefault;
        this.imageUrl = imageUrl;
    }
}