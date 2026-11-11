package com.example.gifserverv2.domain.coin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 코인을 지급한 커밋. 같은 커밋이 여러 브랜치에 푸시되거나 다시 동기화해도 한 번만 지급하기 위해 남긴다. (#250)
 */
@Entity
@Table(name = "rewarded_commit",
        uniqueConstraints = @UniqueConstraint(name = "uk_rewarded_commit_user_sha", columnNames = {"user_id", "sha"}),
        indexes = @Index(name = "idx_rewarded_commit_user_fingerprint", columnList = "user_id, fingerprint"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardedCommit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String sha;

    /** 작성 시각 + 메시지 해시. 리베이스·체리픽으로 SHA 가 바뀐 같은 커밋을 알아보는 데 쓴다 */
    @Column(nullable = false, length = 64)
    private String fingerprint;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RewardedCommit(Long userId, String sha, String fingerprint, Instant createdAt) {
        this.userId = userId;
        this.sha = sha;
        this.fingerprint = fingerprint;
        this.createdAt = createdAt;
    }
}
