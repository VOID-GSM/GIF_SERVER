package com.example.gifserverv2.domain.notice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class NoticeException extends ResponseStatusException {

    public NoticeException(HttpStatus status, String message) {
        super(status, message);
    }

    public static NoticeException notFound() {
        return new NoticeException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다.");
    }

    public static NoticeException titleTooLong() {
        return new NoticeException(HttpStatus.BAD_REQUEST, "제목은 100자를 초과할 수 없습니다.");
    }

    public static NoticeException contentTooLong() {
        return new NoticeException(HttpStatus.BAD_REQUEST, "내용은 2000자를 초과할 수 없습니다.");
    }
}