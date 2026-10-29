package com.example.gifserverv2.domain.item.entity;

import com.example.gifserverv2.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "is_equipped", nullable = false)
    private boolean isEquipped;

    @Builder
    public UserItem(UserEntity user, Item item, boolean isEquipped) {
        this.user = user;
        this.item = item;
        this.isEquipped = isEquipped;
    }

    public void equip() {
        this.isEquipped = true;
    }

    public void unequip() {
        this.isEquipped = false;
    }
}