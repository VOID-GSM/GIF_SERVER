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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String itemKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Column(nullable = false)
    private int price;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder
    public Item(String name, String itemKey, ItemCategory category, int price, boolean isDefault, String imageUrl) {
        this.name = name;
        this.itemKey = itemKey;
        this.category = category;
        this.price = price;
        this.isDefault = isDefault;
        this.imageUrl = imageUrl;
    }
}