package com.example.gifserverv2.domain.project.repository;

import com.example.gifserverv2.domain.project.entity.AdvisorAssignmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvisorAssignmentRequestRepository extends JpaRepository<AdvisorAssignmentRequest, Long> {
    List<AdvisorAssignmentRequest> findAllByProjectIdAndStatus(Long projectId, AdvisorAssignmentRequest.Status status);
}
