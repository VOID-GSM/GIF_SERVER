package com.example.gifserverv2.domain.assessment.catchase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CatChaseException extends ResponseStatusException {

    public CatChaseException(HttpStatus status, String message) {
        super(status, message);
    }

    public static CatChaseException notFound() {
        return new CatChaseException(HttpStatus.NOT_FOUND, "게임 라운드를 찾을 수 없습니다.");
    }

    public static CatChaseException alreadySubmitted() {
        return new CatChaseException(HttpStatus.CONFLICT, "이미 판단 결과를 제출한 라운드입니다.");
    }

    public static CatChaseException invalidPosition() {
        return new CatChaseException(HttpStatus.BAD_REQUEST, "고양이가 없는 위치에 대한 판단입니다.");
    }

    public static CatChaseException judgmentCountMismatch() {
        return new CatChaseException(HttpStatus.BAD_REQUEST, "판단 결과의 개수가 고양이 수와 일치하지 않습니다.");
    }
}
