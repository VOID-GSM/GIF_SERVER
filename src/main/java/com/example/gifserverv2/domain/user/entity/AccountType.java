package com.example.gifserverv2.domain.user.entity;

public enum AccountType {
    STUDENT,
    TEACHER,
    EXTERNAL;

    public static AccountType of(UserEntity user) {
        return switch (user.getEffectiveRole()) {
            case USER -> STUDENT;
            case ADMIN -> TEACHER;
            case EXTERNAL -> EXTERNAL;
        };
    }
}
