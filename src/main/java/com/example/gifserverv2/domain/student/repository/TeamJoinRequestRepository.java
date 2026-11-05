package com.example.gifserverv2.domain.student.repository;

import com.example.gifserverv2.domain.student.entity.TeamJoinRequest;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    boolean existsByUserIdAndStatus(Long userId, Status status);

    List<TeamJoinRequest> findAllByUserIdAndStatus(Long userId, Status status);

    List<TeamJoinRequest> findAllByUserIdInAndStatus(Collection<Long> userIds, Status status);

    List<TeamJoinRequest> findAllByStatusOrderByIdDesc(Status status);

    List<TeamJoinRequest> findAllByProject_IdAndStatusOrderByIdDesc(Long projectId, Status status);
}
