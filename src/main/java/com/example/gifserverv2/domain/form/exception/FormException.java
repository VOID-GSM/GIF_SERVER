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

    public static FormException alreadyAnnounced() {
        return new FormException(HttpStatus.BAD_REQUEST, "이미 공지된 양식은 수정할 수 없습니다.");
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

    public static FormException hasSubmittedAnswers() {
        return new FormException(HttpStatus.CONFLICT, "이미 제출된 답변이 있어 항목을 수정할 수 없습니다.");
    }

    public static FormException titleTooLong() {
        return new FormException(HttpStatus.BAD_REQUEST, "제목은 50자를 초과할 수 없습니다.");
    }

    public static FormException descriptionTooLong() {
        return new FormException(HttpStatus.BAD_REQUEST, "설명은 1000자를 초과할 수 없습니다.");
    }

    public static FormException fieldTitleTooLong() {
        return new FormException(HttpStatus.BAD_REQUEST, "항목 제목은 50자를 초과할 수 없습니다.");
    }

    public static FormException fieldDescriptionTooLong() {
        return new FormException(HttpStatus.BAD_REQUEST, "항목 설명은 200자를 초과할 수 없습니다.");
    }

    public static FormException notProjectMember() {
        return new FormException(HttpStatus.FORBIDDEN, "해당 프로젝트의 팀원만 수정할 수 있습니다.");
    }
}