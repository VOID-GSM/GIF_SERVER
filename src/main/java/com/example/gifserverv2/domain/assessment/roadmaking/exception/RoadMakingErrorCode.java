package com.example.gifserverv2.domain.assessment.roadmaking.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RoadMakingErrorCode {
    ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "검사 라운드를 찾을 수 없습니다."),
    ROUND_ALREADY_FINISHED(HttpStatus.CONFLICT, "이미 종료된 검사입니다."),
    ROUND_TIME_OVER(HttpStatus.CONFLICT, "제한 시간이 종료되었습니다."),
    PROBLEM_NOT_FOUND(HttpStatus.NOT_FOUND, "문제를 찾을 수 없습니다."),
    PROBLEM_ALREADY_SOLVED(HttpStatus.CONFLICT, "이미 해결한 문제입니다."),
    INVALID_FENCE_COUNT(HttpStatus.BAD_REQUEST, "제시된 울타리 개수와 다릅니다."),
    INVALID_FENCE_POSITION(HttpStatus.BAD_REQUEST, "울타리를 놓을 수 없는 위치입니다."),
    PROBLEM_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문제 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}