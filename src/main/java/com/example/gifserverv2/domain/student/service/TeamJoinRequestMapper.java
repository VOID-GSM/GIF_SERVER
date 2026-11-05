package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.student.dto.response.TeamJoinRequestResponse;
import com.example.gifserverv2.domain.student.entity.TeamJoinRequest;
import com.example.gifserverv2.domain.user.entity.UserEntity;
import com.example.gifserverv2.domain.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

final class TeamJoinRequestMapper {

    private TeamJoinRequestMapper() {
    }

    static TeamJoinRequestResponse toResponse(TeamJoinRequest request, UserEntity student) {
        return new TeamJoinRequestResponse(
                request.getId(),
                request.getProject().getId(),
                request.getProject().getName(),
                request.getProject().getTeamName(),
                request.getUserId(),
                student != null ? student.getName() : null,
                student != null ? student.getStudentNumber() : null,
                request.getStatus(),
                request.getRejectReason(),
                request.getCreatedAt());
    }

    static List<TeamJoinRequestResponse> toResponses(List<TeamJoinRequest> requests, UserRepository userRepository) {
        if (requests.isEmpty()) return List.of();
        Map<Long, UserEntity> students = userRepository.findAllById(
                        requests.stream().map(TeamJoinRequest::getUserId).distinct().toList()).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        return requests.stream()
                .map(request -> toResponse(request, students.get(request.getUserId())))
                .toList();
    }
}
