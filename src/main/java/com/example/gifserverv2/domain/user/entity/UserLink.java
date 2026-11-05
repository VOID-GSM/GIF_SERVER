package com.example.gifserverv2.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마이페이지 개인 사이트 링크 (포트폴리오, 블로그 등). 사용자당 최대 5개.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_link", indexes = @Index(name = "idx_user_link_user_id", columnList = "user_id"))
public class UserLink {

    public static final int MAX_LINKS_PER_USER = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 30)
    private String title;

    @Column(nullable = false, length = 500)
    private String url;

    public UserLink(Long userId, String title, String url) {
        this.userId = userId;
        this.title = title;
        this.url = url;
    }

    public void update(String title, String url) {
        this.title = title;
        this.url = url;
    }
}
