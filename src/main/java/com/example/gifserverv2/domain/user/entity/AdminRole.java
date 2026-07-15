package com.example.gifserverv2.domain.user.entity;

public enum AdminRole {
    MAJOR_TEACHER,
    GENERAL_TEACHER,
    MASTER;

    private static final String REQUIRED_SUBJECT_TEACHER_MESSAGE = "선생님 교과 역할을 선택해야 합니다.";
    private static final String INVALID_SUBJECT_TEACHER_MESSAGE = "선생님은 전공 교과 또는 일반 교과 중 하나를 선택해야 합니다.";

    public boolean isSubjectTeacher() {
        return this == MAJOR_TEACHER || this == GENERAL_TEACHER;
    }

    public static boolean isSubjectTeacher(AdminRole adminRole) {
        return adminRole != null && adminRole.isSubjectTeacher();
    }

    public static String subjectTeacherValidationMessage(AdminRole adminRole) {
        if (adminRole == null) {
            return REQUIRED_SUBJECT_TEACHER_MESSAGE;
        }

        // Allow MASTER role to pass validation for administrative APIs
        if (adminRole == MASTER) {
            return null;
        }

        return adminRole.isSubjectTeacher() ? null : INVALID_SUBJECT_TEACHER_MESSAGE;
    }
}
