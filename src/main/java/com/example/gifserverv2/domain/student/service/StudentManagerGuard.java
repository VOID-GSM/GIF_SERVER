package com.example.gifserverv2.domain.student.service;

import com.example.gifserverv2.domain.student.exception.StudentException;
import com.example.gifserverv2.domain.user.entity.AdminRole;
import com.example.gifserverv2.global.security.AuthenticatedUser;

/** 학생 명단·팀 배정 관리는 Master, Void 권한만 할 수 있다. (#136) */
final class StudentManagerGuard {

    private StudentManagerGuard() {
    }

    static boolean isManager(AuthenticatedUser caller) {
        return caller != null && (caller.adminRole() == AdminRole.MASTER || caller.adminRole() == AdminRole.VOID);
    }

    static void requireManager(AuthenticatedUser caller) {
        if (!isManager(caller)) {
            throw StudentException.forbidden();
        }
    }
}
