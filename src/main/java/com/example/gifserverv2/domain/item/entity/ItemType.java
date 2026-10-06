package com.example.gifserverv2.domain.item.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemType {

    NYAH("nyah", "메롱", ItemCategory.EXPRESSION, 200, false),
    SURPRISE("surprise", "놀람", ItemCategory.EXPRESSION, 100, false),
    SAD("sad", "슬픔", ItemCategory.EXPRESSION, 200, false),
    ANGRY("angry", "분노", ItemCategory.EXPRESSION, 200, false),
    TIRED("tired", "피곤", ItemCategory.EXPRESSION, 100, false),
    SMILE("smile", "미소", ItemCategory.EXPRESSION, 0, true), // 기본 아이템

    PERM("perm", "파마", ItemCategory.HAIR, 700, false),
    SHOT("shot", "단발", ItemCategory.HAIR, 600, false),
    BUZZ_CUT("buzz_cut", "밤톨머리", ItemCategory.HAIR, 500, false),
    TAMED_CUT("tamed_cut", "차분한 머리", ItemCategory.HAIR, 500, false),
    SHAGGY_CUT("shaggy_cut", "샤기컷", ItemCategory.HAIR, 600, false),

    DRESS("dress", "원피스", ItemCategory.CLOTH, 700, false),
    SAILOR("sailor", "선원 복", ItemCategory.CLOTH, 1000, false),
    COAT("coat", "코트", ItemCategory.CLOTH, 700, false),
    KNIT_SKIRT("knit_skirt", "니트치마", ItemCategory.CLOTH, 900, false),
    SUIT("suit", "정장", ItemCategory.CLOTH, 800, false),
    SHOULDER_STRAP("shoulder_strap", "멜빵", ItemCategory.CLOTH, 0, true), // 기본 아이템

    RIBBON("ribbon", "리본", ItemCategory.OBJECT, 300, false),
    FLOWER("flower", "꽃", ItemCategory.OBJECT, 200, false),
    BOOK("book", "책", ItemCategory.OBJECT, 400, false),
    MUFFLER("muffler", "목도리", ItemCategory.OBJECT, 300, false),

    CONE("cone", "고깔모자", ItemCategory.DECORATION, 400, false),
    PIN("pin", "머리핀", ItemCategory.DECORATION, 200, false),
    SAILOR_HAT("sailor_hat", "선원모자", ItemCategory.DECORATION, 500, false),

    ROUND_GLASSES("round_glasses", "둥근 안경", ItemCategory.GLASSES, 300, false),
    SUN_GLASSES("sun_glasses", "선글라스", ItemCategory.GLASSES, 400, false);

    private final String itemKey;
    private final String name;
    private final ItemCategory category;
    private final int price;
    private final boolean isDefault;
}