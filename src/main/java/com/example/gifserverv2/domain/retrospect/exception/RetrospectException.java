package com.example.gifserverv2.domain.retrospect.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RetrospectException extends ResponseStatusException {

    public RetrospectException(HttpStatus status, String message) {
        super(status, message);
    }

    public static RetrospectException notFound() {
        return new RetrospectException(HttpStatus.NOT_FOUND, "회고록을 찾을 수 없습니다.");
    }
}