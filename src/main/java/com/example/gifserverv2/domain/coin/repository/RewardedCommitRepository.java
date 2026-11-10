package com.example.gifserverv2.domain.coin.repository;

import com.example.gifserverv2.domain.coin.entity.RewardedCommit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RewardedCommitRepository extends JpaRepository<RewardedCommit, Long> {

    // SHA 나 fingerprint 중 하나라도 같으면 이미 지급한 커밋이다
    @Query("SELECT r FROM RewardedCommit r WHERE r.userId = :userId " +
            "AND (r.sha IN :shas OR r.fingerprint IN :fingerprints)")
    List<RewardedCommit> findAlreadyRewarded(@Param("userId") Long userId,
                                             @Param("shas") Collection<String> shas,
                                             @Param("fingerprints") Collection<String> fingerprints);
}
