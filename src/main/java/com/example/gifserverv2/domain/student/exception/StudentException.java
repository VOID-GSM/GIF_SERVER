package com.example.gifserverv2.domain.student.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StudentException extends ResponseStatusException {

    public StudentException(HttpStatus status, String message) {
        super(status, message);
    }

    public static StudentException forbidden() {
        return new StudentException(HttpStatus.FORBIDDEN, "Master 또는 Void 권한만 학생을 관리할 수 있습니다.");
    }

    public static StudentException studentNotFound() {
        return new StudentException(HttpStatus.NOT_FOUND, "학생을 찾을 수 없습니다.");
    }

    public static StudentException requestNotFound() {
        return new StudentException(HttpStatus.NOT_FOUND, "팀원 추가 요청을 찾을 수 없습니다.");
    }

    public static StudentException requestClosed() {
        return new StudentException(HttpStatus.CONFLICT, "이미 처리된 팀원 추가 요청입니다.");
    }

    public static StudentException alreadyAssigned() {
        return new StudentException(HttpStatus.CONFLICT, "이미 다른 팀에 소속된 학생입니다. 팀 이동을 이용해 주세요.");
    }

    public static StudentException notAssigned() {
        return new StudentException(HttpStatus.BAD_REQUEST, "팀에 소속되지 않은 학생입니다. 팀 배정 요청을 이용해 주세요.");
    }

    public static StudentException leaderCannotLeave() {
        return new StudentException(HttpStatus.BAD_REQUEST, "팀장은 팀을 옮기거나 제외할 수 없습니다. 먼저 다른 팀원을 팀장으로 지정해 주세요.");
    }
}
