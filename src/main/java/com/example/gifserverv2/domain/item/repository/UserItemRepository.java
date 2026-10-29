package com.example.gifserverv2.domain.item.repository;

import com.example.gifserverv2.domain.item.entity.ItemCategory;
import com.example.gifserverv2.domain.item.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    @Query("SELECT ui FROM UserItem ui JOIN FETCH ui.item WHERE ui.user.id = :userId")
    List<UserItem> findAllByUserIdWithItem(@Param("userId") Long userId);

    @Query("SELECT ui.item.id FROM UserItem ui WHERE ui.user.id = :userId")
    List<Long> findItemIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    Optional<UserItem> findByUserIdAndItemId(Long userId, Long itemId);

    @Query("SELECT ui FROM UserItem ui JOIN FETCH ui.item WHERE ui.user.id = :userId AND ui.isEquipped = true")
    List<UserItem> findEquippedItemsByUserId(@Param("userId") Long userId);

    @Query("SELECT ui FROM UserItem ui JOIN FETCH ui.item i WHERE ui.user.id = :userId AND i.category = :category AND ui.isEquipped = true")
    Optional<UserItem> findEquippedItemByUserIdAndCategory(@Param("userId") Long userId, @Param("category") ItemCategory category);
}