package com.example.gifserverv2.domain.form.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FormException extends ResponseStatusException {

    public FormException(HttpStatus status, String message) {
        super(status, message);
    }

    public static FormException notFound() {
        return new FormException(HttpStatus.NOT_FOUND, "양식을 찾을 수 없습니다.");
    }

    public static FormException fieldNotFound() {
        return new FormException(HttpStatus.NOT_FOUND, "양식 항목을 찾을 수 없습니다.");
    }

    public static FormException deadlinePassed() {
        return new FormException(HttpStatus.BAD_REQUEST, "마감일이 지난 양식은 수정할 수 없습니다.");
    }

    public static FormException alreadySubmitted() {
        return new FormException(HttpStatus.CONFLICT, "이미 제출한 양식입니다.");
    }

    public static FormException notAnnounced() {
        return new FormException(HttpStatus.BAD_REQUEST, "공지되지 않은 양식입니다.");
    }

    public static FormException notSubmitted() {
        return new FormException(HttpStatus.NOT_FOUND, "제출한 양식이 없습니다.");
    }

    public static FormException incompleteForm() {
        return new FormException(HttpStatus.BAD_REQUEST, "제목, 마감일, 양식 제목, 설명 중 공백이 있습니다.");
    }
}