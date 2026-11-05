package com.example.gifserverv2.domain.user.repository;

import com.example.gifserverv2.domain.user.entity.UserLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserLinkRepository extends JpaRepository<UserLink, Long> {

    List<UserLink> findAllByUserIdOrderByIdAsc(Long userId);

    long countByUserId(Long userId);

    Optional<UserLink> findByIdAndUserId(Long id, Long userId);
}
