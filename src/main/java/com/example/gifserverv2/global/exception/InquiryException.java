package com.example.gifserverv2.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InquiryException extends ResponseStatusException {

    public InquiryException(HttpStatus status, String message) {
        super(status, message);
    }

    public static InquiryException notFound() {
        return new InquiryException(HttpStatus.NOT_FOUND, "문의를 찾을 수 없습니다.");
    }

    public static InquiryException forbidden() {
        return new InquiryException(HttpStatus.FORBIDDEN, "본인이 등록한 문의만 조회할 수 있습니다.");
    }
    public static InquiryException notMaster() {
        return new InquiryException(HttpStatus.FORBIDDEN, "답변 권한이 없습니다.");
    }
}