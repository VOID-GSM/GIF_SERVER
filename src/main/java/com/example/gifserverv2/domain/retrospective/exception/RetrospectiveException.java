package com.example.gifserverv2.domain.retrospective.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RetrospectiveException extends ResponseStatusException {

    public RetrospectiveException(HttpStatus status, String message) {
        super(status, message);
    }

    public static RetrospectiveException notFound() {
        return new RetrospectiveException(HttpStatus.NOT_FOUND, "활동 기록을 찾을 수 없습니다.");
    }

    public static RetrospectiveException forbidden() {
        return new RetrospectiveException(HttpStatus.FORBIDDEN, "본인이 작성한 활동 기록만 수정하거나 삭제할 수 있습니다.");
    }

    public static RetrospectiveException accessDenied() {
        return new RetrospectiveException(HttpStatus.FORBIDDEN, "비공개로 설정된 활동 기록입니다.");
    }

    public static RetrospectiveException draftNotFound() {
        return new RetrospectiveException(HttpStatus.NOT_FOUND, "임시 저장한 기록을 찾을 수 없습니다.");
    }

    public static RetrospectiveException cannotBookmarkOwn() {
        return new RetrospectiveException(HttpStatus.BAD_REQUEST, "본인이 작성한 활동 기록은 저장할 수 없습니다.");
    }
}
