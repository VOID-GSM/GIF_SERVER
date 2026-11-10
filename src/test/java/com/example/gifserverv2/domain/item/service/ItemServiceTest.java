package com.example.gifserverv2.domain.item.service;

import com.example.gifserverv2.domain.coin.entity.CoinAccount;
import com.example.gifserverv2.domain.coin.repository.CoinAccountRepository;
import com.example.gifserverv2.domain.item.dto.request.BuyItemRequest;
import com.example.gifserverv2.domain.item.dto.request.EquipItemsRequest;
import com.example.gifserverv2.domain.item.dto.response.MyItemsResponse;
import com.example.gifserverv2.domain.item.entity.Item;
import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.repository.ItemRepository;
import com.example.gifserverv2.domain.item.repository.UserItemRepository;
import com.example.gifserverv2.domain.user.entity.Role;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import(ItemService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ItemServiceTest {

    @Autowired ItemService itemService;
    @Autowired UserRepository userRepository;
    @Autowired ItemRepository itemRepository;
    @Autowired UserItemRepository userItemRepository;
    @Autowired CoinAccountRepository coinAccountRepository;

    Long userId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(new UserEntity("s1203@gsm.hs.kr", "김학생", "1203", Role.USER, "1")).getId();
        CoinAccount account = CoinAccount.open(userId);
        account.addCoin(10000);
        coinAccountRepository.save(account);
    }

    @AfterEach
    void tearDown() {
        userItemRepository.deleteAll();
        itemRepository.deleteAll();
        coinAccountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 구매한_아이템은_구매시각이_채워지고_최신순으로_조회된다() {
        Item first = saveItem("first", ItemCategory.values()[0]);
        Item second = saveItem("second", ItemCategory.values()[ItemCategory.values().length > 1 ? 1 : 0]);

        itemService.buyItem(userId, new BuyItemRequest(first.getId()));
        itemService.buyItem(userId, new BuyItemRequest(second.getId()));

        MyItemsResponse result = itemService.getMyItems(userId);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).allSatisfy(item -> assertThat(item.purchasedAt()).isNotNull());
        // 나중에 산 아이템이 먼저 나온다
        assertThat(result.items().get(0).itemId()).isEqualTo(second.getId());
        assertThat(result.items().get(1).itemId()).isEqualTo(first.getId());
        assertThat(result.equipped()).isEmpty();
    }

    @Test
    void 장착한_아이템은_카테고리별로_equipped에_담긴다() {
        Item item = saveItem("hat", ItemCategory.values()[0]);
        itemService.buyItem(userId, new BuyItemRequest(item.getId()));
        itemService.equipItems(userId, new EquipItemsRequest(List.of(item.getId())));

        MyItemsResponse result = itemService.getMyItems(userId);

        assertThat(result.items()).singleElement().satisfies(i -> assertThat(i.equipped()).isTrue());
        assertThat(result.equipped()).containsOnlyKeys(item.getCategory());
        assertThat(result.equipped().get(item.getCategory()).itemId()).isEqualTo(item.getId());
    }

    @Test
    void 보유한_아이템이_없으면_빈_결과를_반환한다() {
        MyItemsResponse result = itemService.getMyItems(userId);

        assertThat(result.items()).isEmpty();
        assertThat(result.equipped()).isEmpty();
    }

    private Item saveItem(String key, ItemCategory category) {
        return itemRepository.save(Item.builder()
                .name(key)
                .itemKey(key)
                .category(category)
                .price(100)
                .isDefault(false)
                .imageUrl("https://files/items/" + key + ".png")
                .build());
    }
}