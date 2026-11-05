package com.example.gifserverv2.domain.student.repository;

import com.example.gifserverv2.domain.student.entity.TeamJoinRequest;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    boolean existsByUserIdAndStatus(Long userId, Status status);

    List<TeamJoinRequest> findAllByUserIdAndStatus(Long userId, Status status);

    List<TeamJoinRequest> findAllByUserIdInAndStatus(Collection<Long> userIds, Status status);

    List<TeamJoinRequest> findAllByStatusOrderByIdDesc(Status status);

    List<TeamJoinRequest> findAllByProject_IdAndStatusOrderByIdDesc(Long projectId, Status status);

    /** 학생이 다른 경로로 팀에 들어갔을 때, 더는 수락할 수 없게 된 대기 요청을 닫는다. */
    default int cancelPendingByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return 0;
        return updateStatusByUserIds(userIds, Status.PENDING, Status.CANCELLED, LocalDateTime.now());
    }

    @Modifying(flushAutomatically = true)
    @Query("UPDATE TeamJoinRequest r SET r.status = :to, r.respondedAt = :now "
            + "WHERE r.userId IN :userIds AND r.status = :from")
    int updateStatusByUserIds(@Param("userIds") Collection<Long> userIds,
                              @Param("from") Status from,
                              @Param("to") Status to,
                              @Param("now") LocalDateTime now);
}
