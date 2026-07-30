package com.example.gifserverv2.domain.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProjectException extends ResponseStatusException {

    public ProjectException(HttpStatus status, String message) {
        super(status, message);
    }

    public static ProjectException notFound() {
        return new ProjectException(HttpStatus.NOT_FOUND, "프로젝트를 찾을 수 없습니다.");
    }

    public static ProjectException notMember() {
        return new ProjectException(HttpStatus.FORBIDDEN, "해당 프로젝트의 멤버가 아닙니다.");
    }

    public static ProjectException notLeader() {
        return new ProjectException(HttpStatus.FORBIDDEN, "팀장만 수행할 수 있습니다.");
    }

    public static ProjectException cannotRemoveLeader() {
        return new ProjectException(HttpStatus.BAD_REQUEST, "팀장은 삭제할 수 없습니다.");
    }

    public static ProjectException alreadyMember() {
        return new ProjectException(HttpStatus.CONFLICT, "이미 팀원으로 등록된 유저입니다.");
    }

    public static ProjectException linkNotFound() {
        return new ProjectException(HttpStatus.NOT_FOUND, "링크를 찾을 수 없습니다.");
    }

    public static ProjectException invalidUrl() {
        return new ProjectException(HttpStatus.BAD_REQUEST, "올바른 URL 형식이 아닙니다. (http:// 또는 https://로 시작해야 합니다)");
    }
}
