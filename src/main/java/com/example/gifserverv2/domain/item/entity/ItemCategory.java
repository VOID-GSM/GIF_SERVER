package com.example.gifserverv2.domain.item.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCategory {
    EXPRESSION("표정"),
    HAIR("머리카락"),
    CLOTH("옷"),
    OBJECT("소품"),
    DECORATION("머리장식"),
    GLASSES("안경");

    private final String description;
}